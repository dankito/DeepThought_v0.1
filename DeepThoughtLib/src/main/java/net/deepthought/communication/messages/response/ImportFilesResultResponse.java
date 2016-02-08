package net.deepthought.communication.messages.response;

import net.deepthought.communication.ConnectorMessagesCreator;
import net.deepthought.communication.messages.MultipartPart;
import net.deepthought.communication.messages.MultipartType;
import net.deepthought.communication.messages.request.MultipartRequest;
import net.deepthought.data.contentextractor.ocr.ImportFilesResult;

/**
 * Created by ganymed on 23/08/15.
 */
public class ImportFilesResultResponse extends MultipartRequest implements ResponseToAsynchronousRequest {

  protected ImportFilesResult result;

  protected int requestMessageId;


  public ImportFilesResultResponse() {
    // for Reflection
  }

  public ImportFilesResultResponse(ImportFilesResult result, int requestMessageId) {
    super();
    this.result = result;
    this.requestMessageId = requestMessageId;

    parts.add(new MultipartPart<String>(ConnectorMessagesCreator.ImportFilesResultMultipartKeyRequestMessageId, MultipartType.Text, Integer.toString(requestMessageId)));
    parts.add(new MultipartPart<ImportFilesResult>(ConnectorMessagesCreator.ImportFilesResultMultipartKeyResponse, MultipartType.Text, result));
    // send ImportFilesResult's binary image data in an extra Multipart
    parts.add(new MultipartPart<byte[]>(ConnectorMessagesCreator.ImportFilesResultMultipartKeyImage, MultipartType.Binary, result.getFileData()));
    result.setFileData(null);
  }


  public ImportFilesResult getResult() {
    return result;
  }

  @Override
  public boolean addPart(MultipartPart part) {
    if(ConnectorMessagesCreator.ImportFilesResultMultipartKeyRequestMessageId.equals(part.getPartName())) {
      if(part.getData() instanceof Long) // don't know why, in Config i specify Integer, but JsonIo parses it to Long
        this.requestMessageId = ((Long)part.getData()).intValue();
      else if(part.getData() instanceof Integer)
        this.requestMessageId = (Integer)part.getData();
    }
    else if(ConnectorMessagesCreator.ImportFilesResultMultipartKeyResponse.equals(part.getPartName()) &&
        part.getData() instanceof ImportFilesResult) {
      this.result = (ImportFilesResult)part.getData();
    }
    else if(ConnectorMessagesCreator.ImportFilesResultMultipartKeyImage.equals(part.getPartName())) {
      if(part.getData() instanceof String && result != null) {
        result.setFileUri((String) part.getData());
      }
    }

    return super.addPart(part);
  }

  @Override
  public int getRequestMessageId() {
    return requestMessageId;
  }

  @Override
  public boolean isDone() {
    if(result != null) {
      return result.isDone();
    }

    return false;
  }
}
