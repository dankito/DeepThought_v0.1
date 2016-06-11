package net.dankito.deepthought.communication.messages.response;

import net.dankito.deepthought.communication.messages.request.Request;

/**
 * Created by ganymed on 23/08/15.
 */
public class ScanBarcodeResultResponse extends Request implements ResponseToAsynchronousRequest {

  protected ScanBarcodeResult scanBarcodeResult;

  protected int requestMessageId;


  public ScanBarcodeResultResponse(ScanBarcodeResult scanBarcodeResult, int requestMessageId) {
    this.scanBarcodeResult = scanBarcodeResult;
    this.requestMessageId = requestMessageId;
  }


  public ScanBarcodeResult getResult() {
    return scanBarcodeResult;
  }


  @Override
  public String toString() {
    return "" + scanBarcodeResult;
  }

  @Override
  public int getRequestMessageId() {
    return requestMessageId;
  }

  @Override
  public boolean isDone() {
//    if(scanBarcodeResult != null) {
//      return scanBarcodeResult.isDone();
//    }
//
//    return false;

    return true;
  }
}
