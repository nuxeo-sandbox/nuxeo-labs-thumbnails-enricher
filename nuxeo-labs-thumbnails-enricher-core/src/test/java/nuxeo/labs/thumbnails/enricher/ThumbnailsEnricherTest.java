package nuxeo.labs.thumbnails.enricher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.io.marshallers.json.AbstractJsonWriterTest;
import org.nuxeo.ecm.core.io.marshallers.json.JsonAssert;
import org.nuxeo.ecm.core.io.marshallers.json.document.DocumentModelJsonWriter;
import org.nuxeo.ecm.core.io.registry.context.DepthValues;
import org.nuxeo.ecm.core.io.registry.context.RenderingContext.CtxBuilder;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.*;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;

@RunWith(FeaturesRunner.class)
@Features({PlatformFeature.class})
@Deploy({"nuxeo.labs.thumbnails.enricher.core.nuxeo-labs-thumbnails-enricher-core"})

public class ThumbnailsEnricherTest extends AbstractJsonWriterTest.Local<DocumentModelJsonWriter, DocumentModel> {

  public ThumbnailsEnricherTest() {
    super(DocumentModelJsonWriter.class, DocumentModel.class);
  }

  @Inject
  private CoreSession session;

  @Test
  public void test() throws Exception {

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

    // Create a Folder
    DocumentModel theFolder = session.createDocumentModel(session.getRootDocument().getPathAsString(), "theFolder", "Folder");
    session.createDocument(theFolder);

    // Create two Picture documents inside the Folder
    DocumentModel note1 = session.createDocumentModel(theFolder.getPathAsString(), "note1", "Note");
    note1.setPropertyValue("dc:title", "note1");
    session.createDocument(note1);

    DocumentModel note2 = session.createDocumentModel(theFolder.getPathAsString(), "note2", "Note");
    note1.setPropertyValue("dc:title", "note2");
    session.createDocument(note2);

    // How do we wait until the thumbnail is created? Or is it synchronous?

    // Test the enricher
    JsonAssert json = jsonAssert(theFolder, CtxBuilder.depth(DepthValues.max).enrich("document", ThumbnailsEnricher.NAME).get());
    json = json.has("contextParameters").isObject();
    // Should be an array of two thumbnail objects returned by the enricher.
    json.has(ThumbnailsEnricher.NAME);

    JsonAssert thumbnails = json.get(ThumbnailsEnricher.NAME);
    thumbnails.isArray();
    JsonAssert pic1thumbnail = thumbnails.get(0);
    pic1thumbnail.has("thumbnailUrl");
    JsonAssert pic2thumbnail = thumbnails.get(1);
    pic2thumbnail.has("thumbnailUrl");
  }
}
