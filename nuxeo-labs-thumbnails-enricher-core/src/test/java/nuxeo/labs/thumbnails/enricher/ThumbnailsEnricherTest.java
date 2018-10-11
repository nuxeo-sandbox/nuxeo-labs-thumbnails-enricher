package nuxeo.labs.thumbnails.enricher;

import org.junit.Before;
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
import org.nuxeo.runtime.transaction.TransactionHelper;

import javax.inject.Inject;

import static nuxeo.labs.thumbnails.enricher.ThumbnailsEnricher.*;

@RunWith(FeaturesRunner.class)
@Features({PlatformFeature.class})
@Deploy({"nuxeo.labs.thumbnails.enricher.core.nuxeo-labs-thumbnails-enricher-core"})

public class ThumbnailsEnricherTest extends AbstractJsonWriterTest.Local<DocumentModelJsonWriter, DocumentModel> {

  public ThumbnailsEnricherTest() {
    super(DocumentModelJsonWriter.class, DocumentModel.class);
  }

  private DocumentModel theFolder;

  @Inject
  protected CoreSession session;

  @Before
  public void setUp() {
    // Create a Folder
    theFolder = session.createDocumentModel(session.getRootDocument().getPathAsString(), "theFolder", "Folder");
    theFolder = session.createDocument(theFolder);

    DocumentModel child_note;
    DocumentModel child_file;
    DocumentModel child_folder;

    // Create three documents inside the Folder
    child_note = session.createDocumentModel(theFolder.getPathAsString(), "note1", "Note");
    child_note.setPropertyValue("dc:title", "note1");
    session.createDocument(child_note);

    child_file = session.createDocumentModel(theFolder.getPathAsString(), "file1", "File");
    child_file.setPropertyValue("dc:title", "file1");
    session.createDocument(child_file);

    child_folder = session.createDocumentModel(theFolder.getPathAsString(), "folder1", "Folder");
    child_folder.setPropertyValue("dc:title", "folder1");
    session.createDocument(child_folder);

    // I usually save the session at the end of the creations
    session.save();

    // If I plan to search on it in sub-tsts, I also commit the transations
    TransactionHelper.commitOrRollbackTransaction();
    TransactionHelper.startTransaction();
  }

  @Test
  // Test default behavior: all children are returned. Since I created 3 children, it should return 3 thumbnails.
  public void enrichFolder_Default_ShouldReturnThreeThumbnails() throws Exception {

    RenderingContext myCtx = CtxBuilder.enrich("document", ThumbnailsEnricher.NAME).get();

    // Test the enricher
    JsonAssert json = jsonAssert(theFolder, myCtx);
    json = json.has("contextParameters").isObject();

    // Should be an array of two thumbnail objects returned by the enricher.
    json.has(ThumbnailsEnricher.NAME);
    JsonAssert thumbnails = json.get(ThumbnailsEnricher.NAME);
    thumbnails.isArray();
    thumbnails.length(3);

    // Should have thumbnail urls
    JsonAssert child = thumbnails.get(0);
    child.has(ThumbnailsEnricher.THUMBNAIL_URL_LABEL);
    child = thumbnails.get(1);
    child.has(ThumbnailsEnricher.THUMBNAIL_URL_LABEL);
    child = thumbnails.get(2);
    child.has(ThumbnailsEnricher.THUMBNAIL_URL_LABEL);
  }

  @Test
  // Test thumbnail-limit 2
  public void enrichFolder_Limit2_ShouldReturnTwoThumbnails() throws Exception {

    RenderingContext myCtx = CtxBuilder.enrich("document", ThumbnailsEnricher.NAME).get();

    myCtx.addParameterValues(THUMBNAILS_HEADER_LIMIT, "2");

    // Test the enricher
    JsonAssert json = jsonAssert(theFolder, myCtx);
    json = json.has("contextParameters").isObject();

    // Should be an array of two thumbnail objects returned by the enricher.
    json.has(ThumbnailsEnricher.NAME);
    JsonAssert thumbnails = json.get(ThumbnailsEnricher.NAME);
    thumbnails.isArray();
    thumbnails.length(2);

    // Should have thumbnail urls
    JsonAssert child1thumbnail = thumbnails.get(0);
    child1thumbnail.has(ThumbnailsEnricher.THUMBNAIL_URL_LABEL);
    JsonAssert child2thumbnail = thumbnails.get(1);
    child2thumbnail.has(ThumbnailsEnricher.THUMBNAIL_URL_LABEL);
  }

  @Test
  // Test thumbnail-type Note,File
  public void enrichFolder_TypeNoteFile_ShouldReturnTwoThumbnails() throws Exception {

    RenderingContext myCtx = CtxBuilder.enrich("document", ThumbnailsEnricher.NAME).get();

    myCtx.addParameterValues(THUMBNAILS_HEADER_TYPES, "Note,File");

    // Test the enricher
    JsonAssert json = jsonAssert(theFolder, myCtx);
    json = json.has("contextParameters").isObject();

    // Should be an array of two thumbnail objects returned by the enricher.
    json.has(ThumbnailsEnricher.NAME);
    JsonAssert thumbnails = json.get(ThumbnailsEnricher.NAME);
    thumbnails.isArray();
    thumbnails.length(2);

    // Should have thumbnail urls
    JsonAssert child1thumbnail = thumbnails.get(0);
    child1thumbnail.has(ThumbnailsEnricher.THUMBNAIL_URL_LABEL);
    JsonAssert child2thumbnail = thumbnails.get(1);
    child2thumbnail.has(ThumbnailsEnricher.THUMBNAIL_URL_LABEL);
  }

  @Test
  // Test thumbnail-facet Folderish
  public void enrichFolder_FacetFolderish_ShouldReturnOneThumbnail() throws Exception {

    RenderingContext myCtx = CtxBuilder.enrich("document", ThumbnailsEnricher.NAME).get();

    myCtx.addParameterValues(THUMBNAILS_HEADER_FACETS, "Folderish");

    // Test the enricher
    JsonAssert json = jsonAssert(theFolder, myCtx);
    json = json.has("contextParameters").isObject();

    // Should be an array of two thumbnail objects returned by the enricher.
    json.has(ThumbnailsEnricher.NAME);
    JsonAssert thumbnails = json.get(ThumbnailsEnricher.NAME);
    thumbnails.isArray();
    thumbnails.length(1);

    // Should have thumbnail urls
    JsonAssert child1thumbnail = thumbnails.get(0);
    child1thumbnail.has(ThumbnailsEnricher.THUMBNAIL_URL_LABEL);
  }

}
