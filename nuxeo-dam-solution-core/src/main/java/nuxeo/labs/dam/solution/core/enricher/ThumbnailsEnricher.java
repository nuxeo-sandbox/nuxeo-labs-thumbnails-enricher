package nuxeo.labs.dam.solution.core.enricher;

import org.codehaus.jackson.JsonGenerator;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.marshallers.json.enrichers.AbstractJsonEnricher;
import org.nuxeo.ecm.core.io.registry.reflect.Setup;

import java.io.IOException;

import static org.nuxeo.ecm.core.io.registry.reflect.Instantiations.SINGLETON;
import static org.nuxeo.ecm.core.io.registry.reflect.Priorities.REFERENCE;

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
 *     "thumbnails": { ... }
 *   }
 * }
 * </pre>
 * </p>
 */
@Setup(mode = SINGLETON, priority = REFERENCE)
public class ThumbnailsEnricher extends AbstractJsonEnricher<DocumentModel> {

  public static final String NAME = "thumbnails";

  public ThumbnailsEnricher() {
    super(NAME);
  }

  @Override
  public void write(JsonGenerator jg, DocumentModel theDeliverable) throws IOException {

    /* Here's the old Automation Script:

    function run(input, params) {
      var deliverable = Document.GetParent(input, {'type': 'Deliverable' });
      if (typeof deliverable === "undefined" || deliverable===null) {
        return input;
      }
      var thumbnails = [];
      //get all assets
      var assets = Document.GetChildren(deliverable, {});
      assets.forEach(function(asset) {
        if (asset['picture:views'] ) {
              thumbnails.push(asset['picture:views'][0].content);
        } else if (asset['thumb:thumbnail']) {
               thumbnails.push(asset['thumb:thumbnail']);
        }
      });
      deliverable['deliverable:assets_thumbnails'] = thumbnails;
      Document.Save(deliverable, {});
    }
    */

    // Get children that have a useful thumbnail? Or just all children, at least to start.

    jg.writeFieldName(NAME);
    jg.writeStartArray();
    jg.writeStartObject();
    jg.writeStringField("name", (String) theDeliverable.getPropertyValue("dc:title"));
    jg.writeStringField("thumbnailUrl", "http://foo.com/bar");
    jg.writeEndObject();
    jg.writeEndArray();
  }
}
