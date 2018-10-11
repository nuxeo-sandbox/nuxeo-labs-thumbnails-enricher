package nuxeo.labs.thumbnails.enricher;

import org.codehaus.jackson.JsonGenerator;
import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.io.marshallers.json.enrichers.AbstractJsonEnricher;
import org.nuxeo.ecm.core.io.registry.reflect.Setup;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.nuxeo.ecm.core.io.registry.reflect.Instantiations.SINGLETON;
import static org.nuxeo.ecm.core.io.registry.reflect.Priorities.REFERENCE;
import static org.nuxeo.ecm.core.schema.FacetNames.FOLDERISH;

/**
 * Enrich {@link nuxeo.ecm.core.api.DocumentModel} Json.
 * <p>
 * Format is:
 *
 * <pre>
 * {@code
 * {
 *   ...
 *   "contextParameters": {
 *     "thumbnails": [
 *       {
 *         "id": ...
 *         "thumbnailUrl": ...
 *       },
 *       ...
 *     ]
 *   }
 * }
 * </pre>
 * </p>
 */
@Setup(mode = SINGLETON, priority = REFERENCE)
public class ThumbnailsEnricher extends AbstractJsonEnricher<DocumentModel> {

  // This is the name of the enricher
  public static final String NAME = "thumbnails";

  // These values are used in the returned JSON as keys
  public static final String THUMBNAIL_URL_LABEL = "thumbnailUrl";
  public static final String THUMBNAIL_ID_LABEL = "id";

  // These optional values can be passed as HTTP headers
  public static final String THUMBNAILS_HEADER_LIMIT = "thumbnail-limit";
  public static final String THUMBNAILS_HEADER_TYPES = "thumbnail-types";
  public static final String THUMBNAILS_HEADER_FACETS = "thumbnail-facets";

  // This is copied from org.nuxeo.ecm.platform.thumbnail.io.ThumbnailJsonEnricher.java
  private static final String THUMBNAIL_URL_PATTERN = "%s/api/v1/repo/%s/id/%s/@rendition/thumbnail";

  public ThumbnailsEnricher() {
    super(NAME);
  }

  @Override
  public void write(JsonGenerator jg, DocumentModel theFolderish) throws IOException {

    if (theFolderish.hasFacet(FOLDERISH)) {

      CoreSession session = theFolderish.getCoreSession();

      String thumbnailLimitString = ctx.getParameter(THUMBNAILS_HEADER_LIMIT);
      String thumbnailTypesString = ctx.getParameter(THUMBNAILS_HEADER_TYPES);
      String thumbnailFacetsString = ctx.getParameter(THUMBNAILS_HEADER_FACETS);

      String theQuery = "SELECT * FROM Document WHERE ecm:parentId = '" + theFolderish.getId() + "'";

      // Filter children by type
      if (thumbnailTypesString != null) {
        theQuery = theQuery + " AND ecm:primaryType IN (" + csvToQuoted(thumbnailTypesString) + ")";
      }

      // Filter children by facet
      if (thumbnailFacetsString != null) {
        theQuery = theQuery + " AND ecm:mixinType IN (" + csvToQuoted(thumbnailFacetsString) + ")";
      }

      DocumentModelList theChildren;
      if (thumbnailLimitString != null) {
        theChildren = session.query(theQuery, Integer.parseInt(thumbnailLimitString));
      } else {
        theChildren = session.query(theQuery);
      }

      long limit = theChildren.totalSize();

      // Write JSON response.
      jg.writeFieldName(NAME);
      jg.writeStartArray();

      for (int i = 0; i < limit; i++) {

        DocumentModel currentChild = theChildren.get(i);

        jg.writeStartObject();
        jg.writeStringField(THUMBNAIL_ID_LABEL, (String) currentChild.getId());
        // This is copied from org.nuxeo.ecm.platform.thumbnail.io.ThumbnailJsonEnricher.java because can't figure out
        // how to use an existing enricher from Java.
        jg.writeStringField(THUMBNAIL_URL_LABEL,
            String.format(THUMBNAIL_URL_PATTERN, ctx.getBaseUrl().replaceAll("/$", ""),
                currentChild.getRepositoryName(), currentChild.getId()));
        jg.writeEndObject();
      }

      jg.writeEndArray();
    }
  }

  /**
   * Add single quotes to each item in a CSV string.
   */
  private String csvToQuoted(String csv) {
    List<String> valuesArray = Arrays.asList(StringUtils.split(StringUtils.trim(csv), ","));
    String quotedCsv = valuesArray.stream()
        .map(s -> "'" + s + "'")
        .collect(Collectors.joining(","));
    return quotedCsv;
  }
}
