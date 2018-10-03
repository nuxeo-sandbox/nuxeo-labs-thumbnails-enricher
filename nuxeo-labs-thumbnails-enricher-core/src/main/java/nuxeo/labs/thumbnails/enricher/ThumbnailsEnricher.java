package nuxeo.labs.thumbnails.enricher;

import org.codehaus.jackson.JsonGenerator;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.io.marshallers.json.enrichers.AbstractJsonEnricher;
import org.nuxeo.ecm.core.io.registry.reflect.Setup;

import java.io.IOException;

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

  public static final String NAME = "thumbnails";

  public static final String THUMBNAIL_URL_LABEL = "thumbnailUrl";

  public static final String THUMBNAIL_URL_PATTERN = "%s/api/v1/repo/%s/id/%s/@rendition/thumbnail";

  public ThumbnailsEnricher() {
    super(NAME);
  }

  @Override
  public void write(JsonGenerator jg, DocumentModel theFolderish) throws IOException {

    if (theFolderish.hasFacet(FOLDERISH)) {

      CoreSession session = theFolderish.getCoreSession();

      // Get children that have a useful thumbnail? Or just all children, at least to start
      DocumentModelList theChildren = session.getChildren(theFolderish.getRef());

      jg.writeFieldName(NAME);
      jg.writeStartArray();

      for (int i = 0; i < theChildren.totalSize(); i++) {

        DocumentModel currentChild = theChildren.get(i);

        jg.writeStartObject();
        jg.writeStringField("id", (String) currentChild.getId());
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
}
