package net.dankito.deepthought.data.helper;

/**
 * Created by ganymed on 01/01/15.
 */
public class ErrorOccurred {

    protected boolean errorOccurred = false;

    public boolean hasErrorOccurred() {
      return errorOccurred;
    }

    public void setErrorOccurred(boolean errorOccurred) {
      this.errorOccurred = errorOccurred;
    }

    @Override
    public String toString() {
      return "Error occurred: " + errorOccurred;
    }
}
