package net.deepthought.communication.messages.response;

import net.deepthought.communication.ConnectorMessagesCreator;
import net.deepthought.communication.messages.MultipartPart;
import net.deepthought.communication.messages.MultipartType;
import net.deepthought.communication.messages.request.MultipartRequest;
import net.deepthought.data.contentextractor.ocr.CaptureImageResult;

/**
 * Created by ganymed on 23/08/15.
 */
public class CaptureImageResultResponse extends MultipartRequest implements ResponseToAsynchronousRequest {

  protected CaptureImageResult result;

  protected int requestMessageId;


  public CaptureImageResultResponse() {
    // for Reflection
  }

  public CaptureImageResultResponse(CaptureImageResult result, int requestMessageId) {
    super();
    this.result = result;
    this.requestMessageId = requestMessageId;

    parts.add(new MultipartPart<String>(ConnectorMessagesCreator.CaptureImageResultMultipartKeyRequestMessageId, MultipartType.Text, Integer.toString(requestMessageId)));
    parts.add(new MultipartPart<CaptureImageResult>(ConnectorMessagesCreator.CaptureImageResultMultipartKeyResponse, MultipartType.Text, result));
    // send CaptureImageResult's binary image data in an extra Multipart
    parts.add(new MultipartPart<byte[]>(ConnectorMessagesCreator.CaptureImageResultMultipartKeyImage, MultipartType.Binary, result.getImageData()));
    result.setImageData(null);
  }


  public CaptureImageResult getResult() {
    return result;
  }

  @Override
  public boolean addPart(MultipartPart part) {
    if(ConnectorMessagesCreator.CaptureImageResultMultipartKeyRequestMessageId.equals(part.getPartName())) {
      if(part.getData() instanceof Long) // don't know why, in Config i specify Integer, but JsonIo parses it to Long
        this.requestMessageId = ((Long)part.getData()).intValue();
      else if(part.getData() instanceof Integer)
        this.requestMessageId = (Integer)part.getData();
    }
    else if(ConnectorMessagesCreator.CaptureImageResultMultipartKeyResponse.equals(part.getPartName()) &&
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
