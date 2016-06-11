package net.dankito.deepthought.communication.messages.response;

import net.dankito.deepthought.util.Result;

/**
 * Created by ganymed on 07/12/15.
 */
public class ScanBarcodeResult extends Result {

  protected String decodedBarcode = null;

  protected String barcodeFormat = null;


  public ScanBarcodeResult(Exception error) {
    super(error);
  }

  public ScanBarcodeResult(String decodedBarcode) {
    this(decodedBarcode, null);
  }

  public ScanBarcodeResult(String decodedBarcode, String barcodeFormat) {
    super(decodedBarcode != null);

    this.decodedBarcode = decodedBarcode;
    this.barcodeFormat = barcodeFormat;
  }


  public String getDecodedBarcode() {
    return decodedBarcode;
  }

  public String getBarcodeFormat() {
    return barcodeFormat;
  }

}
