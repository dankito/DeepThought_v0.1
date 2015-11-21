package net.deepthought.communication.messages.response;

import net.deepthought.communication.ConnectorMessagesCreator;
import net.deepthought.communication.messages.MultipartPart;
import net.deepthought.communication.messages.request.MultipartRequest;
import net.deepthought.data.contentextractor.ocr.CaptureImageResult;

/**
 * Created by ganymed on 23/08/15.
 */
public class CaptureImageResultResponse extends MultipartRequest {

  protected CaptureImageResult result;


  public CaptureImageResultResponse() {
    // for Reflection
  }

  public CaptureImageResultResponse(CaptureImageResult result, int messageId) {
    super();
    this.result = result;
    this.messageId = messageId;
  }


  public CaptureImageResult getResult() {
    return result;
  }

  @Override
  public boolean addPart(MultipartPart part) {
    if(ConnectorMessagesCreator.CaptureImageResultMultipartKeyResponse.equals(part.getPartName()) &&
        part.getData() instanceof CaptureImageResult) {
      this.result = (CaptureImageResult)part.getData();
    }
    else if(ConnectorMessagesCreator.CaptureImageResultMultipartKeyImage.equals(part.getPartName())) {
      if(part.getData() instanceof String && result != null) {
        result.setImageUri((String)part.getData());
      }
    }

    return super.addPart(part);
  }
}
