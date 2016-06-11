package net.dankito.deepthought.communication.messages;

import net.dankito.deepthought.communication.ConnectorMessagesCreator;
import net.dankito.deepthought.communication.messages.request.ImportFilesRequest;
import net.dankito.deepthought.communication.messages.request.StopRequestWithAsynchronousResponse;
import net.dankito.deepthought.communication.messages.response.ImportFilesResultResponse;
import net.dankito.deepthought.communication.messages.response.ScanBarcodeResultResponse;
import net.dankito.deepthought.communication.model.DoOcrConfiguration;
import net.dankito.deepthought.communication.model.ImportFilesConfiguration;
import net.dankito.deepthought.data.contentextractor.ocr.ImportFilesResult;
import net.dankito.deepthought.communication.Addresses;
import net.dankito.deepthought.communication.messages.request.DoOcrRequest;
import net.dankito.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.dankito.deepthought.communication.messages.response.OcrResultResponse;

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

    allowedMethods.add(new WebMethodConfig(Addresses.StartImportFilesMethodName, ImportFilesRequest.class, getImportFilesMultipartConfig()));
    allowedMethods.add(new WebMethodConfig(Addresses.ImportFilesResultMethodName, ImportFilesResultResponse.class, getImportFilesResultMultipartConfig()));
    allowedMethods.add(new WebMethodConfig(Addresses.StopImportFilesMethodName, StopRequestWithAsynchronousResponse.class));

    allowedMethods.add(new WebMethodConfig(Addresses.DoOcrOnImageMethodName, DoOcrRequest.class, getDoOcrMultipartConfig()));
    allowedMethods.add(new WebMethodConfig(Addresses.OcrResultMethodName, OcrResultResponse.class));
    allowedMethods.add(new WebMethodConfig(Addresses.StopDoOcrOnImageMethodName, StopRequestWithAsynchronousResponse.class));

    allowedMethods.add(new WebMethodConfig(Addresses.StartScanBarcodeMethodName, RequestWithAsynchronousResponse.class));
    allowedMethods.add(new WebMethodConfig(Addresses.ScanBarcodeResultMethodName, ScanBarcodeResultResponse.class));
    allowedMethods.add(new WebMethodConfig(Addresses.StopScanBarcodeMethodName, StopRequestWithAsynchronousResponse.class));

    return allowedMethods;
  }


  protected List<MultipartPart> getImportFilesMultipartConfig() {
    List<MultipartPart> multipartPartsConfig = new ArrayList<>();

    multipartPartsConfig.add(new MultipartPart(ConnectorMessagesCreator.ImportFilesMultipartKeyConfiguration, MultipartType.Text, ImportFilesConfiguration.class));

    return multipartPartsConfig;
  }

  protected List<MultipartPart> getDoOcrMultipartConfig() {
    List<MultipartPart> multipartPartsConfig = new ArrayList<>();

    multipartPartsConfig.add(new MultipartPart(ConnectorMessagesCreator.DoOcrMultipartKeyConfiguration, MultipartType.Text, DoOcrConfiguration.class));
    multipartPartsConfig.add(new MultipartPart(ConnectorMessagesCreator.DoOcrMultipartKeyImage, MultipartType.Binary, String.class));

    return multipartPartsConfig;
  }

  protected List<MultipartPart> getImportFilesResultMultipartConfig() {
    List<MultipartPart> multipartPartsConfig = new ArrayList<>();

    multipartPartsConfig.add(new MultipartPart(ConnectorMessagesCreator.ImportFilesResultMultipartKeyRequestMessageId, MultipartType.Text, Integer.class));
    multipartPartsConfig.add(new MultipartPart(ConnectorMessagesCreator.ImportFilesResultMultipartKeyResponse, MultipartType.Text, ImportFilesResult.class));
    multipartPartsConfig.add(new MultipartPart(ConnectorMessagesCreator.ImportFilesResultMultipartKeyImage, MultipartType.Binary, String.class));

    return multipartPartsConfig;
  }

}
