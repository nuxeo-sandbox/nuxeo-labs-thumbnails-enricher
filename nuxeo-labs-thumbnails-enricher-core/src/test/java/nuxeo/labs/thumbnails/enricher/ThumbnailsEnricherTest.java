package nuxeo.labs.thumbnails.enricher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.marshallers.json.AbstractJsonWriterTest;
import org.nuxeo.ecm.core.io.marshallers.json.JsonAssert;
import org.nuxeo.ecm.core.io.marshallers.json.document.DocumentModelJsonWriter;
import org.nuxeo.ecm.core.io.registry.context.RenderingContext.CtxBuilder;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;

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

    // Create a Folder
    DocumentModel theFolder = session.createDocumentModel(session.getRootDocument().getPathAsString(), "theFolder", "Folder");
    session.createDocument(theFolder);

    // Create two documents inside the Folder
    DocumentModel child1 = session.createDocumentModel(theFolder.getPathAsString(), "note1", "Note");
    child1.setPropertyValue("dc:title", "note1");
    session.createDocument(child1);

    DocumentModel child2 = session.createDocumentModel(theFolder.getPathAsString(), "note2", "Note");
    child2.setPropertyValue("dc:title", "note2");
    session.createDocument(child2);

    // Test the enricher
    JsonAssert json = jsonAssert(theFolder, CtxBuilder.enrich("document", ThumbnailsEnricher.NAME).get());
    json = json.has("contextParameters").isObject();

    // Should be an array of two thumbnail objects returned by the enricher.
    json.has(ThumbnailsEnricher.NAME);
    JsonAssert thumbnails = json.get(ThumbnailsEnricher.NAME);
    thumbnails.isArray();

    // Should have thumbnail urls
    JsonAssert child1thumbnail = thumbnails.get(0);
    child1thumbnail.has(ThumbnailsEnricher.THUMBNAIL_URL_LABEL);
    JsonAssert child2thumbnail = thumbnails.get(1);
    child2thumbnail.has(ThumbnailsEnricher.THUMBNAIL_URL_LABEL);
  }
}
