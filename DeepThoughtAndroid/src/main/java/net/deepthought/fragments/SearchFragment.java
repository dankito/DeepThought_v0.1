package net.deepthought.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import net.deepthought.Application;
import net.deepthought.R;
import net.deepthought.activities.ActivityManager;
import net.deepthought.adapter.SearchForTagsAdapter;
import net.deepthought.adapter.SearchResultsAdapter;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 02/10/14.
 */
public class SearchFragment extends Fragment implements SearchForTagsAdapter.TagsToSearchForChangedListener {

  public enum TextCompareOption {
    Contains(0),
    StartsWith(1),
    EndsWith(2),
    Equals(3);

    int index;

    TextCompareOption(int index) {
      this.index = index;
    }

    public static TextCompareOption fromIndex(int index) {
      switch(index) {
        case 0:
          return Contains;
        case 1:
          return StartsWith;
        case 2:
          return EndsWith;
        default:
          return Equals;
      }
    }
  }

  protected RelativeLayout rlytSearchTag = null;
  protected TextView txtvwTagsToSearchFor = null;
  protected Button btnSearchTags = null;
  protected ListView lstvwSearchTags = null;

  protected List<Tag> tagsToSearchFor = new ArrayList<>();

  protected RelativeLayout rlytSearchTitle = null;
  protected Spinner spnSearchTitleSearchOption = null;
  protected EditText edtxtSearchTitle = null;

  protected RelativeLayout rlytSearchContent = null;

  protected SearchResultsAdapter searchResultsAdapter = null;


  public SearchFragment() {

  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_search, container, false);

    setupSearchTagSection(rootView);

    setupSearchTitleSection(rootView);

    setupSearchContentSection(rootView);

    ListView lstvwSearchResults = (ListView)rootView.findViewById(R.id.lstvwSearchResults);
    searchResultsAdapter = new SearchResultsAdapter(this.getActivity());
    lstvwSearchResults.setAdapter(searchResultsAdapter);
    lstvwSearchResults.setOnItemClickListener(lstvwSearchResultsOnItemClickListener);

    return rootView;
  }

  protected void setupSearchTagSection(View rootView) {
    TextView txtvwSearchSectionTitleTags = (TextView)rootView.findViewById(R.id.txtvwSearchSectionTitleTags);
    txtvwSearchSectionTitleTags.setOnClickListener(txtvwSearchSectionTitleTagsOnClickListener);
    rlytSearchTag = (RelativeLayout)rootView.findViewById(R.id.rlytSearchTag);
    rlytSearchTag.setOnClickListener(txtvwSearchSectionTitleTagsOnClickListener);

    txtvwTagsToSearchFor = (TextView)rootView.findViewById(R.id.txtvwSearchTagsToSearchFor);

    btnSearchTags = (Button)rootView.findViewById(R.id.btnSearchTags);
    btnSearchTags.setOnClickListener(btnSearchTagsOnClickListener);

    EditText edtxtSearchTagsFilter = (EditText)rootView.findViewById(R.id.edtxtSearchTagsFilter);
    edtxtSearchTagsFilter.addTextChangedListener(edtxtSearchTagsFilterTextChangedListener);

    lstvwSearchTags = (ListView)rootView.findViewById(R.id.lstvwSearchTags);
    lstvwSearchTags.setAdapter(new SearchForTagsAdapter(this.getActivity(), tagsToSearchFor, this));
  }

  protected void setupSearchTitleSection(View rootView) {
    TextView txtvwSearchSectionTitleTitle = (TextView)rootView.findViewById(R.id.txtvwSearchSectionTitleTitle);
    txtvwSearchSectionTitleTitle.setOnClickListener(txtvwSearchSectionTitleTitleOnClickListener);
    rlytSearchTitle = (RelativeLayout)rootView.findViewById(R.id.rlytSearchTitle);
    rlytSearchTitle.setOnClickListener(txtvwSearchSectionTitleTitleOnClickListener);

    spnSearchTitleSearchOption = (Spinner)rootView.findViewById(R.id.spnSearchTitleSearchOption);
    spnSearchTitleSearchOption.setOnItemSelectedListener(spnSearchTitleSearchOptionOnItemSelectedListener);

    List<String> titleSearchOptions = new ArrayList<>();
    titleSearchOptions.add(getString(R.string.search_contains_text));
    titleSearchOptions.add(getString(R.string.search_starts_with_text));
    titleSearchOptions.add(getString(R.string.search_ends_with_text));
    titleSearchOptions.add(getString(R.string.search_equals_text));
    ArrayAdapter<String> spnSearchTitleSearchOptionAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, titleSearchOptions);
    spnSearchTitleSearchOptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spnSearchTitleSearchOption.setAdapter(spnSearchTitleSearchOptionAdapter);

    edtxtSearchTitle = (EditText)rootView.findViewById(R.id.edtxtSearchTitle);
    edtxtSearchTitle.addTextChangedListener(edtxtSearchTitleOnTextChangedListener);
  }

  protected void setupSearchContentSection(View rootView) {
    TextView txtvwSearchSectionContentTitle = (TextView)rootView.findViewById(R.id.txtvwSearchSectionContentTitle);
    txtvwSearchSectionContentTitle.setOnClickListener(txtvwSearchSectionContentTitleOnClickListener);
    rlytSearchContent = (RelativeLayout)rootView.findViewById(R.id.rlytSearchContent);
    rlytSearchContent.setOnClickListener(txtvwSearchSectionContentTitleOnClickListener);
  }


  protected View.OnClickListener txtvwSearchSectionTitleTagsOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      if(rlytSearchTag.getVisibility() == View.GONE)
        rlytSearchTag.setVisibility(View.VISIBLE);
      else
        rlytSearchTag.setVisibility(View.GONE);
    }
  };

  protected View.OnClickListener btnSearchTagsOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      searchForTagsAsync(tagsToSearchFor);
    }
  };

  protected TextWatcher edtxtSearchTagsFilterTextChangedListener = new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
      ((SearchForTagsAdapter) lstvwSearchTags.getAdapter()).getFilter().filter(s.toString());
    }
  };

  @Override
  public void tagsToSearchForChanged(List<Tag> tagsToSearchFor) {
    btnSearchTags.setEnabled(tagsToSearchFor.size() > 0);

    String tagsText = "";
    for(Tag tag : tagsToSearchFor)
      tagsText += tag.getName() + " & ";

    if(tagsText.length() > 2)
      tagsText = tagsText.substring(0, tagsText.length() - 3);

    if(tagsText.length() == 0)
      tagsText = getString(R.string.search_select_tags);

    txtvwTagsToSearchFor.setText(tagsText);

    searchForTagsAsync(tagsToSearchFor); // TODO: search immediately for wait till search button is pressed?
  }

  protected void searchForTagsAsync(List<Tag> tagsToSearchFor) {
    new AsyncTask<List<Tag>, Void, Void>() {

      @Override
      protected Void doInBackground(List<Tag>... params) {
        searchForTags(params[0]);
        return null;
      }
    }.execute(tagsToSearchFor);
  }

  protected void searchForTags(List<Tag> tagsToSearchFor) {
    List<Entry> searchResults = null;

    if(Application.getDeepThought() != null)
      searchResults = Application.getDeepThought().findEntriesByTags(tagsToSearchFor);
    else
      searchResults = new ArrayList<>();

    searchResultsAdapter.setSearchResults(searchResults);
  }


  protected View.OnClickListener txtvwSearchSectionTitleTitleOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      if(rlytSearchTitle.getVisibility() == View.GONE)
        rlytSearchTitle.setVisibility(View.VISIBLE);
      else
        rlytSearchTitle.setVisibility(View.GONE);
    }
  };

  protected AdapterView.OnItemSelectedListener spnSearchTitleSearchOptionOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
      searchForTitleAsync();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
  };

  protected TextWatcher edtxtSearchTitleOnTextChangedListener = new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
      searchForTitleAsync();
    }
  };

  protected void searchForTitleAsync() {
    final String titleToSearchFor = edtxtSearchTitle.getText().toString().toLowerCase();
    final TextCompareOption textCompareOption = TextCompareOption.fromIndex(spnSearchTitleSearchOption.getSelectedItemPosition());

    new AsyncTask<Void, Void, List<Entry>>() {

      @Override
      protected List<Entry> doInBackground(Void... params) {
        return filterEntriesForTitle(titleToSearchFor, textCompareOption);
      }

      @Override
      protected void onPostExecute(List<Entry> searchResults) {
        searchResultsAdapter.setSearchResults(searchResults);
        super.onPostExecute(searchResults);
      }
    }.execute();
  }

  protected void searchForTitle() {
    String titleToSearchFor = edtxtSearchTitle.getText().toString().toLowerCase();
    TextCompareOption textCompareOption = TextCompareOption.fromIndex(spnSearchTitleSearchOption.getSelectedItemPosition());
    List<Entry> searchResults = filterEntriesForTitle(titleToSearchFor, textCompareOption);

    searchResultsAdapter.setSearchResults(searchResults);
  }

  protected List<Entry> filterEntriesForTitle(String titleToSearchFor, TextCompareOption textCompareOption) {
    List<Entry> searchResults = new ArrayList<>();

    if(Application.getDeepThought() == null)
      return searchResults;

    switch(textCompareOption) {
      case Contains:
        for(Entry entry : Application.getDeepThought().getEntries()) {
          if(entry.getTitle().toLowerCase().contains(titleToSearchFor))
            searchResults.add(entry);
        }
        break;
      case StartsWith:
        for(Entry entry : Application.getDeepThought().getEntries()) {
          if(entry.getTitle().toLowerCase().startsWith(titleToSearchFor))
            searchResults.add(entry);
        }
        break;
      case EndsWith:
        for(Entry entry : Application.getDeepThought().getEntries()) {
          if(entry.getTitle().toLowerCase().endsWith(titleToSearchFor))
            searchResults.add(entry);
        }
        break;
      case Equals:
        for(Entry entry : Application.getDeepThought().getEntries()) {
          if(entry.getTitle().toLowerCase().equals(titleToSearchFor))
            searchResults.add(entry);
        }
        break;
    }
    return searchResults;
  }


  protected View.OnClickListener txtvwSearchSectionContentTitleOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      if(rlytSearchContent.getVisibility() == View.GONE)
        rlytSearchContent.setVisibility(View.VISIBLE);
      else
        rlytSearchContent.setVisibility(View.GONE);
    }
  };


  protected AdapterView.OnItemClickListener lstvwSearchResultsOnItemClickListener = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      Entry searchResult = searchResultsAdapter.getSearchResultAt(position);
      ActivityManager.getInstance().showEditEntryActivity(searchResult);
    }
  };
}
