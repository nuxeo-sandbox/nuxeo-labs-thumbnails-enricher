package nuxeo.labs.thumbnails.enricher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.marshallers.json.AbstractJsonWriterTest;
import org.nuxeo.ecm.core.io.marshallers.json.JsonAssert;
import org.nuxeo.ecm.core.io.marshallers.json.document.DocumentModelJsonWriter;
import org.nuxeo.ecm.core.io.registry.context.RenderingContext;
import org.nuxeo.ecm.core.io.registry.context.RenderingContext.CtxBuilder;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;

import static nuxeo.labs.thumbnails.enricher.ThumbnailsEnricher.THUMBNAIL_HEADER_LIMIT;

@RunWith(FeaturesRunner.class)
@Features({PlatformFeature.class})
@Deploy({"nuxeo.labs.thumbnails.enricher.core.nuxeo-labs-thumbnails-enricher-core"})

public class ThumbnailsEnricherTest extends AbstractJsonWriterTest.Local<DocumentModelJsonWriter, DocumentModel> {

  public ThumbnailsEnricherTest() {
    super(DocumentModelJsonWriter.class, DocumentModel.class);
  }

  @Inject
  protected CoreSession session;

  @Test
  public void test() throws Exception {

    // Create a Folder
    DocumentModel theFolder = session.createDocumentModel(session.getRootDocument().getPathAsString(), "theFolder", "Folder");
    theFolder = session.createDocument(theFolder);

    // Create two documents inside the Folder
    DocumentModel child1 = session.createDocumentModel(theFolder.getPathAsString(), "note1", "Note");
    child1.setPropertyValue("dc:title", "note1");
    session.createDocument(child1);

    DocumentModel child2 = session.createDocumentModel(theFolder.getPathAsString(), "note2", "Note");
    child2.setPropertyValue("dc:title", "note2");
    session.createDocument(child2);

    RenderingContext myCtx = CtxBuilder.enrich("document", ThumbnailsEnricher.NAME).get();

    myCtx.addParameterValues(THUMBNAIL_HEADER_LIMIT, "2");

    // Test the enricher
    JsonAssert json = jsonAssert(theFolder, myCtx);
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
