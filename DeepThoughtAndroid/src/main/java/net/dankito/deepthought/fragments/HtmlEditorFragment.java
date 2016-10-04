package net.dankito.deepthought.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import net.dankito.deepthought.R;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.controls.html.AndroidHtmlEditor;
import net.dankito.deepthought.controls.html.AndroidHtmlEditorPool;
import net.dankito.deepthought.controls.html.IHtmlEditorListener;

/**
 * Created by ganymed on 27/09/16.
 */
public class HtmlEditorFragment extends Fragment implements ICleanUp {

  protected AndroidHtmlEditor htmlEditor;

  protected IHtmlEditorListener htmlEditorListener;


  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_html_editor, container, false);

    setupHtmlEditor(rootView);

    return rootView;
  }

  protected void setupHtmlEditor(View rootView) {
    RelativeLayout rlytHtmlEditorContent = (RelativeLayout)rootView.findViewById(R.id.rlytHtmlEditorContent);

    htmlEditor = AndroidHtmlEditorPool.getInstance().getHtmlEditor(getActivity(), htmlEditorListener);

    rlytHtmlEditorContent.addView(htmlEditor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    setHtmlEditorLayoutParams();
  }

  protected void setHtmlEditorLayoutParams() {
    RelativeLayout.LayoutParams contentEditorParams = (RelativeLayout.LayoutParams)htmlEditor.getLayoutParams();
    contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
    contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

    htmlEditor.setLayoutParams(contentEditorParams);
  }


  @Override
  public void cleanUp() {
    AndroidHtmlEditorPool.getInstance().htmlEditorReleased(htmlEditor);
  }


  public AndroidHtmlEditor getHtmlEditor() {
    return htmlEditor;
  }

  public String getHtml() {
    return htmlEditor.getHtml();
  }

  public void setHtml(String html) {
    htmlEditor.setHtml(html);
  }

  public void setHtmlEditorListener(IHtmlEditorListener htmlEditorListener) {
    this.htmlEditorListener = htmlEditorListener;
  }

}
