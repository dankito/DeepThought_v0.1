package net.deepthought.communication.messages;

import net.deepthought.communication.Addresses;
import net.deepthought.communication.ConnectorMessagesCreator;
import net.deepthought.communication.messages.request.CaptureImageOrDoOcrRequest;
import net.deepthought.communication.messages.request.StopCaptureImageOrDoOcrRequest;
import net.deepthought.communication.messages.response.CaptureImageResultResponse;
import net.deepthought.communication.messages.response.OcrResultResponse;
import net.deepthought.communication.model.DoOcrConfiguration;
import net.deepthought.data.contentextractor.ocr.CaptureImageResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 20/11/15.
 */
public class DeepThoughtMessagesReceiverConfig extends DefaultMessagesReceiverConfig {


  public DeepThoughtMessagesReceiverConfig(int port, AsynchronousResponseListenerManager listenerManager) {
    super(port, Addresses.DeepThoughtUriPart, listenerManager);
  }


  @Override
  protected List<WebMethodConfig> getDefaultAllowedMethods() {
    List<WebMethodConfig> allowedMethods = super.getDefaultAllowedMethods();

    allowedMethods.add(new WebMethodConfig(Addresses.StartCaptureImageAndDoOcrMethodName, CaptureImageOrDoOcrRequest.class, getStartCaptureImageAndDoOcrMultipartConfig()));
    allowedMethods.add(new WebMethodConfig(Addresses.CaptureImageResultMethodName, CaptureImageResultResponse.class, getCaptureImageResultMultipartConfig()));
    allowedMethods.add(new WebMethodConfig(Addresses.OcrResultMethodName, OcrResultResponse.class));
    allowedMethods.add(new WebMethodConfig(Addresses.StopCaptureImageAndDoOcrMethodName, StopCaptureImageOrDoOcrRequest.class));

    return allowedMethods;
  }


  protected List<MultipartPart> getStartCaptureImageAndDoOcrMultipartConfig() {
    List<MultipartPart> multipartPartsConfig = new ArrayList<>();

    multipartPartsConfig.add(new MultipartPart(ConnectorMessagesCreator.DoOcrMultipartKeyConfiguration, MultipartType.Text, DoOcrConfiguration.class));
    multipartPartsConfig.add(new MultipartPart(ConnectorMessagesCreator.DoOcrMultipartKeyImage, MultipartType.Binary, String.class));

    return multipartPartsConfig;
  }

  protected List<MultipartPart> getCaptureImageResultMultipartConfig() {
    List<MultipartPart> multipartPartsConfig = new ArrayList<>();

    multipartPartsConfig.add(new MultipartPart(ConnectorMessagesCreator.CaptureImageResultMultipartKeyResponse, MultipartType.Text, CaptureImageResult.class));
    multipartPartsConfig.add(new MultipartPart(ConnectorMessagesCreator.CaptureImageResultMultipartKeyImage, MultipartType.Binary, String.class));

    return multipartPartsConfig;
  }

}
