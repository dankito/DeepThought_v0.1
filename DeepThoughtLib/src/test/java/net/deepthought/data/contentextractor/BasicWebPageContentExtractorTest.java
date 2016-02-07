package net.deepthought.data.contentextractor;

import net.deepthought.data.html.JsoupHtmlHelper;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Reference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by ganymed on 05/12/15.
 */
public class BasicWebPageContentExtractorTest {


  protected BasicWebPageContentExtractor webPageContentExtractor;


  @Before
  public void setup() {
    webPageContentExtractor = new BasicWebPageContentExtractor(new JsoupHtmlHelper());
  }


  @Test
  public void drecksTool_DownloadsPageSuccessfully() {
    String url = "http://www.dreckstool.de";
    Assert.assertTrue(webPageContentExtractor.canCreateEntryFromUrl(url));

    Assert.assertEquals(url, webPageContentExtractor.getSiteBaseUrl());
    Assert.assertEquals(url + "/favicon.ico", webPageContentExtractor.getIconUrl());

    EntryCreationResult result = webPageContentExtractor.createEntryFromArticle(url);
    Assert.assertTrue(result.successful());
    Assert.assertNotNull(result.getCreatedEntry());
    Assert.assertNotNull(result.getReference());

    Reference reference = result.getReference();
    Assert.assertEquals(url, reference.getOnlineAddress());
    Assert.assertEquals(reference.getCreatedOn(), reference.getLastAccessDate());

    Entry entry = result.getCreatedEntry();
    Assert.assertEquals(DrecksToolHomePageContent, entry.getContent());
  }


  @Test
  public void coberturaAtFreecode_DownloadsPageSuccessfully() {
    String url = "http://freecode.com/projects/cobertura"; // this site is not updated anymore
    String baseUrl = "http://freecode.com";
    Assert.assertTrue(webPageContentExtractor.canCreateEntryFromUrl(url));

    Assert.assertEquals(baseUrl, webPageContentExtractor.getSiteBaseUrl());
    Assert.assertEquals(baseUrl + "/favicon.ico", webPageContentExtractor.getIconUrl());

    EntryCreationResult result = webPageContentExtractor.createEntryFromArticle(url);
    Assert.assertTrue(result.successful());
    Assert.assertNotNull(result.getCreatedEntry());
    Assert.assertNotNull(result.getReference());

    Reference reference = result.getReference();
    Assert.assertEquals(url, reference.getOnlineAddress());
    Assert.assertEquals(reference.getCreatedOn(), reference.getLastAccessDate());

    Entry entry = result.getCreatedEntry();
    Assert.assertEquals(CoberturaAtFreecodeContent, entry.getContent());
  }


  protected static final String DrecksToolHomePageContent = "<!--?xml version=\"1.0\" encoding=\"ISO-8859-1\"?--><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" " +
      "\"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
      "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en-US\" xml:lang=\"en-US\">\n" +
      " <head> \n" +
      "  <title>www.dreckstool.de</title> \n" +
      "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\"> \n" +
      "  <link rel=\"SHORTCUT ICON\" href=\"http://www.dreckstool.de/favicon.ico\"> \n" +
      "  <link rel=\"icon\" type=\"image/x-icon\" href=\"http://www.dreckstool.de/favicon.ico\"> \n" +
      "  <style type=\"text/css\">@import url(http://www.dreckstool.de/style.css);</style> \n" +
      " </head> \n" +
      " <body> \n" +
      "  <div id=\"head\"> \n" +
      "   <a href=\"http://www.dreckstool.de/index.do\" title=\"www.dreckstool.de\"><img src=\"http://www.dreckstool.de/dreckstool.png\" title=\"www.dreckstool.de\"></a> \n" +
      "  </div> \n" +
      "  <h1>Willkommen bei www.dreckstool.de</h1> \n" +
      "  <p class=\"textblock\"> <a href=\"http://www.dreckstool.de/index.do\" title=\"www.dreckstool.de\">www.dreckstool.de</a> hat es sich zur Aufgabe gemacht, den genervten Nutzern allerlei verbreiteter Computer-Programme ein Forum zu bieten, in dem sie ihrem Ärger über diese Tools Luft machen können. Durch einfachen Klick auf den \"Dreckstool!\"-Button in unserer Hitliste kann dem betreffenden Produkt die persönliche Missgunst mitgeteilt werden, wodurch es automatisch einen Punkt weiter nach oben in der Liste der schlechtesten Tools aller Zeiten steigt. </p> \n" +
      "  <p class=\"textblock\"> Die Liste stellt keinesfalls eine ernsthafte objektive Bewertung der Tools dar, sondern spiegelt lediglich das Abstimmverhalten der Besucher wieder. Ein Besuch der Site soll in erster Linie Spass machen und dient keinerlei wissenschaftlichen Auswertungen über Softwarequalität und Ergonomie der dort aufgeführten Tools oder irgendwelchen kommerziellen Interessen. Auch Betriebssysteme werden hier trotz mehrfacher Anfragen nicht aufgeführt. </p> \n" +
      "  <p class=\"textblock\"> Hurmorlose Zeitgenossen sollten die Seiten am Besten erst garnicht besuchen, sondern in den dunklen Winkel des Internets zurückkehren, aus dem sie gekommen sind. Wer einen Rechtschreibfehler findet, darf ihn behalten ;-) </p> \n" +
      "  <p class=\"bigentrance\"> [ <a href=\"http://www.dreckstool.de/hitlist.do\" title=\"enter www.dreckstool.de\">ENTER</a> ] </p> \n" +
      "  <p class=\"textblock\"> Die Inhalte anderer, evtl. durch Links verbundener Domänen, stehen in der Verantwortung deren Betreiber. Alle in dieser Web-Site verwendeten Markennamen, eingetragenen Warenzeichen, graphischen Firmen- oder Produkt-Logos und Schutzmarken sind Eigentum der jeweiligen Rechte-Inhaber. Veröffentlichungen erfolgen ohne die explizite Kennzeichnung eines evtl. Patent- und Markenschutzes. </p> \n" +
      "  <div id=\"foot\"> \n" +
      "   <p class=\"foottext\"> Version 0.3.2<br> © Team Dreckstool 2000-2015 </p> \n" +
      "   <a href=\"http://www.digits.com/\"><img src=\"http://counter.digits.com/wc/-c/1/-f/3FFC00/dreckstool\"></a> \n" +
      "   <br> \n" +
      "   <a href=\"http://v1.nedstatbasic.net/stats?AAWY3Aov8aDvbEs20MMSigWozk4g\"><img src=\"http://m1.nedstatbasic.net/n?id=AAWY3Aov8aDvbEs20MMSigWozk4g\" height=\"18\" width=\"18\"></a> \n" +
      "  </div>  \n" +
      " </body>\n" +
      "</html>";

  protected static final String CoberturaAtFreecodeContent = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
      "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
      " <head> \n" +
      "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"> \n" +
      "  <title>Cobertura – Freecode</title> \n" +
      "  <link href=\"http://a.fsdn.com/fm/stylesheets/base_packaged.css?1386111739\" media=\"screen\" rel=\"stylesheet\" type=\"text/css\"> \n" +
      "  <!--[if IE]>\n" +
      "    <link href=\"http://a.fsdn.com/fm/stylesheets/ie.css?1386111726\" media=\"screen\" rel=\"stylesheet\" type=\"text/css\" />\n" +
      "    <![endif]--> \n" +
      "  <meta property=\"og:title\" content=\"Cobertura\"> \n" +
      "  <meta property=\"og:description\" content=\"A Java code-coverage reporting application.\"> \n" +
      "  <meta property=\"og:image\" content=\"/screenshots/e3/89/e389d275a44d3683dc7cf671520bf4d3_thumb.jpg?1360741849\"> \n" +
      "  <meta name=\"description\" content=\"A Java code-coverage reporting application.\"> \n" +
      "  <meta name=\"keywords\" content=\"Software Development, Testing, Quality Assurance\"> \n" +
      "  <meta name=\"google-site-verification\" content=\"LuyhdevGU_34uCpdk-MN6giihtzaQ50K89IEnf2GkQ0\"> \n" +
      "  <script language=\"JavaScript\" type=\"text/javascript\">\n" +
      "      var immersion_adcode = '';\n" +
      "      if (!window.FC) { window.FC = {}; }\n" +
      "\n" +
      "      FC.loadAd = function(id, src) {\n" +
      "        var isImmersion = id.indexOf('immersion') >= 0;\n" +
      "        if (immersion_adcode && !isImmersion) {\n" +
      "          return;\n" +
      "        }\n" +
      "\n" +
      "        document.write('<scr' + 'ipt type=\"text/javascript\" src=\"' + src + '\"><\\/scr' + 'ipt>');\n" +
      "      }\n" +
      "\n" +
      "      dfp_ord=Math.random()*10000000000000000;\n" +
      "      dfp_tile = 1;\n" +
      "\n" +
      "      window.google_analytics_uacct = \"UA-32013-4\";\n" +
      "\n" +
      "      var _gaq = _gaq || [];\n" +
      "\n" +
      "      _gaq.push(\n" +
      "          ['_setAccount', 'UA-32013-4'],\n" +
      "          ['_trackPageview'],\n" +
      "          ['t2._setAccount', 'UA-36126581-1'],\n" +
      "          ['t2._trackPageview']\n" +
      "      );\n" +
      "\n" +
      "      (function() {\n" +
      "        var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;\n" +
      "        ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';\n" +
      "        var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);\n" +
      "      })();\n" +
      "    </script> \n" +
      " </head> \n" +
      " <body> \n" +
      "  <div id=\"page\"> \n" +
      "   <div id=\"topnav\"> \n" +
      "    <div class=\"topnav-container clearfix\"> \n" +
      "     <div class=\"top-login\">\n" +
      "      <center>\n" +
      "       Effective 2014-06-18 Freecode is no longer being updated (content may be stale). \n" +
      "       <a href=\"http://freecode.com/about\">»Learn More</a>\n" +
      "      </center>\n" +
      "     </div> \n" +
      "    </div> \n" +
      "   </div> \n" +
      "   <div id=\"container\"> \n" +
      "    <div id=\"header\" class=\"clearfix\"> \n" +
      "     <div id=\"leaderboard\"> \n" +
      "      <div class=\"banner1\"> \n" +
      "       <!-- DoubleClick Ad Tag --> \n" +
      "       <script language=\"JavaScript\" type=\"text/javascript\">\n" +
      "  FC.loadAd('main_p1_leader', document.location.protocol+'//ad.doubleclick.net/adj/ostg.freshmeat/main_p1_leader;pg=%2Fprojects%2Fcobertura;logged_in=0;sz=728x90;tile='+dfp_tile+';ord='+dfp_ord+'?');\n" +
      "   dfp_tile++;\n" +
      "</script> \n" +
      "       <!-- End DoubleClick Ad Tag --> \n" +
      "      </div> \n" +
      "      <div class=\"logo\"> \n" +
      "       <a href=\"http://freecode.com/\"><img alt=\"Freecode Logo\" src=\"http://a.fsdn.com/fm/images/fm_logo.png?1386111726\"></a> \n" +
      "      </div> \n" +
      "     </div> \n" +
      "     <div class=\"clearfix\"></div> \n" +
      "     <div class=\"navbarblock\"> \n" +
      "      <div class=\"navbar\"> \n" +
      "       <span class=\"navleft\"></span> \n" +
      "       <ul class=\"navigation\"> \n" +
      "        <li><a href=\"http://freecode.com/\">Home</a></li> \n" +
      "        <li><a href=\"http://freecode.com/articles\">Articles</a></li> \n" +
      "        <li><a href=\"http://freecode.com/tags\">Browse Projects by Tag</a></li> \n" +
      "        <!-- <li><a href=\"/projects/new\">Submit new Project</a></li> --> \n" +
      "        <li><a href=\"http://freecode.com/about\">About</a></li> \n" +
      "        <li><a href=\"http://freecode.com/blog\">Blog</a></li> \n" +
      "        <!-- <li><a href=\"http://help.freecode.com/\" onclick=\"window.open(this.href);return false;\">Help</a></li> --> \n" +
      "        <li class=\"sites bubbleInfo\"><a href=\"http://freecode.com/#\" class=\"trigger\">Sites</a> \n" +
      "         <div class=\"popup\"> \n" +
      "          <span class=\"p1\"><img alt=\"\" src=\"http://a.fsdn.com/fm/images/fm_sites_menu_01.gif?1386111726\"></span> \n" +
      "          <span class=\"p2\"></span> \n" +
      "          <div class=\"menuitems\"> \n" +
      "           <ul class=\"sites-sub\"> \n" +
      "            <li><a href=\"http://freecode.com\">All</a></li> \n" +
      "            <li><a href=\"http://mac.freecode.com\">Mac OS X</a></li> \n" +
      "            <li><a href=\"http://mobile.freecode.com\">Mobile</a></li> \n" +
      "            <li><a href=\"http://unix.freecode.com\">Unix</a></li> \n" +
      "           </ul> \n" +
      "          </div> \n" +
      "          <span class=\"p3\"><img alt=\"\" src=\"http://a.fsdn.com/fm/images/fm_sites_menu_03.gif?1386111726\"></span> \n" +
      "         </div> </li> \n" +
      "       </ul> \n" +
      "      </div> \n" +
      "     </div> \n" +
      "    </div> \n" +
      "    <div class=\"crumbs\">\n" +
      "     <a href=\"http://freecode.com/projects\">Projects</a> / \n" +
      "     <span>Cobertura</span>\n" +
      "    </div> \n" +
      "    <div class=\"clearfix\"> \n" +
      "     <h1> Cobertura</h1> \n" +
      "     <div class=\"project-detail\"> \n" +
      "      <p>Cobertura is a Java tool that calculates the percentage of code accessed by tests. It can be used to identify which parts of your Java program are lacking test coverage. It is based on jcoverage.</p> \n" +
      "      <table id=\"project-tag-cloud\"> \n" +
      "       <tbody>\n" +
      "        <tr>\n" +
      "         <th><span>Tags</span></th> \n" +
      "         <td><a href=\"http://freecode.com/tags/software-development\" class=\"tagSize3\" rel=\"tag\">Software Development</a> <a href=\"http://freecode.com/tags/testing\" class=\"tagSize3\" rel=\"tag\">Testing</a> <a href=\"http://freecode.com/tags/quality-assurance\" class=\"tagSize3\" rel=\"tag\">Quality Assurance</a> </td>\n" +
      "        </tr> \n" +
      "        <tr>\n" +
      "         <th><span>Licenses</span></th> \n" +
      "         <td><a href=\"http://freecode.com/tags/gnu-general-public-license-gpl\" class=\"tagSize3\" rel=\"tag\">GPL</a> </td>\n" +
      "        </tr> \n" +
      "        <tr>\n" +
      "         <th><span>Operating Systems</span></th> \n" +
      "         <td><a href=\"http://freecode.com/tags/os-independent\" class=\"tagSize3\" rel=\"tag\">OS Independent</a> </td>\n" +
      "        </tr> \n" +
      "        <tr>\n" +
      "         <th><span>Implementation</span></th> \n" +
      "         <td><a href=\"http://freecode.com/tags/java\" class=\"tagSize3\" rel=\"tag\">Java</a> </td>\n" +
      "        </tr> \n" +
      "       </tbody>\n" +
      "      </table> \n" +
      "      <div class=\"clearfix project-cta\"> \n" +
      "       <div class=\"width50\" id=\"more_info_download\"> \n" +
      "        <div class=\"moreinfolink\"> \n" +
      "         <a href=\"http://freecode.com/urls/946a5316ff9712b47f3bd3b392f480a8\" onclick=\"_gaq.push(['_trackEvent', 'Projects', 'Website', 'Cobertura']);\" title=\"Website at cobertura.sourceforge.net\">Website</a>\n" +
      "        </div> \n" +
      "        <div class=\"downloadlink\"> \n" +
      "         <a href=\"http://freecode.com/urls/563918f2255eaf72f15a1068c945924a\" class=\"\" onclick=\"_gaq.push(['_trackEvent', 'Projects', 'Download', 'Cobertura']);\" title=\"Zip at prdownloads.sourceforge.net\">Download</a>\n" +
      "        </div> \n" +
      "       </div> \n" +
      "       <div class=\"width50\" id=\"social-sharing\"> \n" +
      "        <div class=\"google-plus\"> \n" +
      "         <div class=\"g-plusone\" data-size=\"medium\"></div> \n" +
      "         <script type=\"text/javascript\">\n" +
      "          (function() {\n" +
      "           var po = document.createElement('script'); po.type = 'text/javascript'; po.async = true;\n" +
      "           po.src = 'https://apis.google.com/js/plusone.js';\n" +
      "           var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);\n" +
      "           })();\n" +
      "  </script> \n" +
      "        </div> \n" +
      "        <div class=\"twitter\"> \n" +
      "         <a href=\"http://twitter.com/share\" class=\"twitter-share-button\" data-text=\"A Java code-coverage reporting application.\" data-count=\"horizontal\" data-via=\"freecode_com\">Tweet</a> \n" +
      "         <script type=\"text/javascript\" src=\"http://platform.twitter.com/widgets.js\"></script> \n" +
      "        </div> \n" +
      "       </div> \n" +
      "      </div> \n" +
      "      <div class=\"clearfix\"> \n" +
      "       <div class=\"recent-announcements\"> \n" +
      "       </div> \n" +
      "       <div class=\"recent-releases\"> \n" +
      "        <h2> \n" +
      "         <ul class=\"recents-sub-navigation\"> \n" +
      "          <!-- <li><a href=\"/projects/cobertura/tags\">Release tags</a></li> --> \n" +
      "          <li><a href=\"http://freecode.com/projects/cobertura/releases\">All releases</a></li> \n" +
      "         </ul>Recent releases </h2> \n" +
      "        <div class=\"release clearfix\" id=\"release_255104\"> \n" +
      "         <div class=\"clearfix\"> \n" +
      "          <ul class=\"release-badge\"> \n" +
      "           <li class=\"before\"><img alt=\"\" src=\"http://a.fsdn.com/fm/images/rrelease-before.png?1386111726\"></li> \n" +
      "           <li class=\"release\"><a href=\"http://freecode.com/projects/cobertura/releases/255104\">1.9</a></li> \n" +
      "          </ul> \n" +
      "          <ul class=\"rdate\"> \n" +
      "           <li><img alt=\"\" src=\"http://a.fsdn.com/fm/images/rrelease-mid.png?1386111726\"></li> \n" +
      "           <li class=\"rdate\">&nbsp;06 Jun 2007 07:33</li> \n" +
      "           <li><img alt=\"\" src=\"http://a.fsdn.com/fm/images/rrelease-after.png?1386111726\"></li> \n" +
      "          </ul> \n" +
      "          <div class=\"meta clearfix\"> \n" +
      "           <ul class=\"tagList\"></ul> \n" +
      "          </div> \n" +
      "         </div> \n" +
      "         <p class=\"truncate changes\"><strong>Release Notes:</strong> Branch/conditional coverage is greatly improved. A \"maxmemory\" attribute was added to the ant tasks. Support for Maven and similar environments was improved. Various bugs were fixed. </p>\n" +
      "        </div> \n" +
      "        <div class=\"release clearfix\" id=\"release_224654\"> \n" +
      "         <div class=\"clearfix\"> \n" +
      "          <ul class=\"release-badge\"> \n" +
      "           <li class=\"before\"><img alt=\"\" src=\"http://a.fsdn.com/fm/images/rrelease-before.png?1386111726\"></li> \n" +
      "           <li class=\"release\"><a href=\"http://freecode.com/projects/cobertura/releases/224654\">1.8</a></li> \n" +
      "          </ul> \n" +
      "          <ul class=\"rdate\"> \n" +
      "           <li><img alt=\"\" src=\"http://a.fsdn.com/fm/images/rrelease-mid.png?1386111726\"></li> \n" +
      "           <li class=\"rdate\">&nbsp;10 Apr 2006 19:38</li> \n" +
      "           <li><img alt=\"\" src=\"http://a.fsdn.com/fm/images/rrelease-after.png?1386111726\"></li> \n" +
      "          </ul> \n" +
      "          <div class=\"meta clearfix\"> \n" +
      "           <ul class=\"tagList\"></ul> \n" +
      "          </div> \n" +
      "         </div> \n" +
      "         <p class=\"truncate changes\"><strong>Release Notes:</strong> New features include showing the total number of lines and branches in the HTML report, support for Groovy, allowing multiple JVMs to cleanly write to the same data file, and more robust instrumentation of archives (including archives inside of archives). </p>\n" +
      "        </div> \n" +
      "        <div class=\"release clearfix\" id=\"release_213892\"> \n" +
      "         <div class=\"clearfix\"> \n" +
      "          <ul class=\"release-badge\"> \n" +
      "           <li class=\"before\"><img alt=\"\" src=\"http://a.fsdn.com/fm/images/rrelease-before.png?1386111726\"></li> \n" +
      "           <li class=\"release\"><a href=\"http://freecode.com/projects/cobertura/releases/213892\">1.7</a></li> \n" +
      "          </ul> \n" +
      "          <ul class=\"rdate\"> \n" +
      "           <li><img alt=\"\" src=\"http://a.fsdn.com/fm/images/rrelease-mid.png?1386111726\"></li> \n" +
      "           <li class=\"rdate\">&nbsp;06 Dec 2005 15:17</li> \n" +
      "           <li><img alt=\"\" src=\"http://a.fsdn.com/fm/images/rrelease-after.png?1386111726\"></li> \n" +
      "          </ul> \n" +
      "          <div class=\"meta clearfix\"> \n" +
      "           <ul class=\"tagList\"></ul> \n" +
      "          </div> \n" +
      "         </div> \n" +
      "         <p class=\"truncate changes\"><strong>Release Notes:</strong> The merge task was fixed. Changes that allow Cobertura to work alongside AspectJ were also made. A possible bug when using instrumented classes in Tomcat was fixed. </p>\n" +
      "        </div> \n" +
      "        <div class=\"release clearfix\" id=\"release_204791\"> \n" +
      "         <div class=\"clearfix\"> \n" +
      "          <ul class=\"release-badge\"> \n" +
      "           <li class=\"before\"><img alt=\"\" src=\"http://a.fsdn.com/fm/images/rrelease-before.png?1386111726\"></li> \n" +
      "           <li class=\"release\"><a href=\"http://freecode.com/projects/cobertura/releases/204791\">1.6</a></li> \n" +
      "          </ul> \n" +
      "          <ul class=\"rdate\"> \n" +
      "           <li><img alt=\"\" src=\"http://a.fsdn.com/fm/images/rrelease-mid.png?1386111726\"></li> \n" +
      "           <li class=\"rdate\">&nbsp;22 Aug 2005 18:10</li> \n" +
      "           <li><img alt=\"\" src=\"http://a.fsdn.com/fm/images/rrelease-after.png?1386111726\"></li> \n" +
      "          </ul> \n" +
      "          <div class=\"meta clearfix\"> \n" +
      "           <ul class=\"tagList\"></ul> \n" +
      "          </div> \n" +
      "         </div> \n" +
      "         <p class=\"truncate changes\"><strong>Release Notes:</strong> The instrumenting and reporting tasks now both support using multiple filesets to specify classes and source code, respectively. There were also a few minor bugfixes. </p>\n" +
      "        </div> \n" +
      "        <div class=\"release clearfix\" id=\"release_203419\"> \n" +
      "         <div class=\"clearfix\"> \n" +
      "          <ul class=\"release-badge\"> \n" +
      "           <li class=\"before\"><img alt=\"\" src=\"http://a.fsdn.com/fm/images/rrelease-before.png?1386111726\"></li> \n" +
      "           <li class=\"release\"><a href=\"http://freecode.com/projects/cobertura/releases/203419\">1.5</a></li> \n" +
      "          </ul> \n" +
      "          <ul class=\"rdate\"> \n" +
      "           <li><img alt=\"\" src=\"http://a.fsdn.com/fm/images/rrelease-mid.png?1386111726\"></li> \n" +
      "           <li class=\"rdate\">&nbsp;05 Aug 2005 20:20</li> \n" +
      "           <li><img alt=\"\" src=\"http://a.fsdn.com/fm/images/rrelease-after.png?1386111726\"></li> \n" +
      "          </ul> \n" +
      "          <div class=\"meta clearfix\"> \n" +
      "           <ul class=\"tagList\"></ul> \n" +
      "          </div> \n" +
      "         </div> \n" +
      "         <p class=\"truncate changes\"><strong>Release Notes:</strong> You can now instrument classes inside of a jar, war, ear, etc. The \"cobertura-check\" Ant task has been greatly improved. The \"cobertura-merge\" task and various other bugs have been fixed. </p>\n" +
      "        </div> \n" +
      "       </div> \n" +
      "       <div class=\"recent-comments\"> \n" +
      "       </div> \n" +
      "      </div> \n" +
      "     </div> \n" +
      "     <div class=\"sbblock\"> \n" +
      "      <div class=\"sidebar first\"> \n" +
      "       <div class=\"sidebar-content\"> \n" +
      "        <h3>Links</h3> \n" +
      "        <ul class=\"sub-navigation divide\">\n" +
      "         <a name=\"downloads\"></a> \n" +
      "         <li class=\"url \" title=\"Bug Tracker - sourceforge.net\"><a href=\"http://freecode.com/urls/7bd0e2494ffdec75d42b96466fcec1c2\">Bug Tracker</a></li> \n" +
      "         <li class=\"url \" title=\"Demo site - cobertura.sourceforge.net\"><a href=\"http://freecode.com/urls/0c1c631261de07fda0e6175f66bf9ef0\">Demo site</a></li> \n" +
      "         <li class=\"url \" title=\"Mailinglist - lists.sourceforge.net\"><a href=\"http://freecode.com/urls/0c84d60d51162f3bfacb4d39fd720fe1\">Mailinglist</a></li> \n" +
      "         <li class=\"url \" title=\"Tar/BZ2 - prdownloads.sourceforge.net\"><a href=\"http://freecode.com/urls/0a69dc121bd43e8ff3b5a0894a874218\">Tar/BZ2</a></li> \n" +
      "         <li class=\"url \" title=\"Tar/GZ - prdownloads.sourceforge.net\"><a href=\"http://freecode.com/urls/8f8e4b3594736b67d21e1984384aec1d\">Tar/GZ</a></li> \n" +
      "         <li class=\"url website\" title=\"Website - cobertura.sourceforge.net\"><a href=\"http://freecode.com/urls/946a5316ff9712b47f3bd3b392f480a8\">Website</a></li> \n" +
      "         <li class=\"url \" title=\"Zip - prdownloads.sourceforge.net\"><a href=\"http://freecode.com/urls/563918f2255eaf72f15a1068c945924a\">Zip</a></li> \n" +
      "        </ul> \n" +
      "       </div> \n" +
      "      </div> \n" +
      "      <div class=\"sidebar\"> \n" +
      "       <div class=\"sidebar-content\"> \n" +
      "        <div class=\"submitter clearfix\"> \n" +
      "         <ul> \n" +
      "          <li class=\"avatar\"><a href=\"http://freecode.com/users/thekingant\" class=\"avatar\" title=\"thekingant\"><img alt=\"Avatar\" src=\"http://www.gravatar.com/avatar/5c4504ef12733cf352a75d4c53f1461b?rating=PG&amp;size=30\"></a></li> \n" +
      "          <li><p><a href=\"http://freecode.com/users/thekingant\" class=\"submitter\">thekingant</a><br> <span>11 Jan 2001 15:23</span></p></li> \n" +
      "         </ul> \n" +
      "        </div> \n" +
      "        <div class=\"sidebar-screen\"> \n" +
      "        </div> \n" +
      "        <ul class=\"sub-navigation split\"> \n" +
      "         <li class=\"dependency\"><a href=\"http://freecode.com/projects/cobertura/dependencies\">Dependencies</a></li> \n" +
      "         <!-- <li class=\"report-problem\"><a href=\"/projects/cobertura/tickets/new\">Report problem</a></li> --> \n" +
      "        </ul> \n" +
      "        <ul class=\"sub-navigation split\"> \n" +
      "         <li class=\"graphs\"><a href=\"http://freecode.com/projects/cobertura/date_metrics\">Graphs</a></li> \n" +
      "        </ul> \n" +
      "        <div class=\"project-filter\"> \n" +
      "        </div> \n" +
      "       </div> \n" +
      "      </div> \n" +
      "      <div class=\"sidebar\"> \n" +
      "      </div> \n" +
      "      <div class=\"banner-imu\"> \n" +
      "       <!-- DoubleClick Ad Tag --> \n" +
      "       <script language=\"JavaScript\" type=\"text/javascript\">\n" +
      "  FC.loadAd('main_p6_imu', document.location.protocol+'//ad.doubleclick.net/adj/ostg.freshmeat/main_p6_imu;pg=%2Fprojects%2Fcobertura;logged_in=0;sz=300x250,300x600;tile='+dfp_tile+';ord='+dfp_ord+'?');\n" +
      "   dfp_tile++;\n" +
      "</script> \n" +
      "       <!-- End DoubleClick Ad Tag --> \n" +
      "      </div> \n" +
      "     </div> \n" +
      "    </div> \n" +
      "   </div> \n" +
      "  </div> \n" +
      "  <div id=\"mashup\"> \n" +
      "   <div class=\"mashup-container clearfix\"> \n" +
      "    <div class=\"col-1\"> \n" +
      "     <a href=\"http://freecode.com/projects/openstack4j\"><img alt=\"Screenshot\" class=\"screenshotThumb\" src=\"http://a.fsdn.com/fm/images/no-screenshot.png?1386111726\"></a> \n" +
      "     <h2>Project Spotlight</h2> \n" +
      "     <h3><a href=\"http://freecode.com/projects/openstack4j\">OpenStack4j</a></h3> \n" +
      "     <p>A Fluent OpenStack client API for Java.</p> \n" +
      "    </div> \n" +
      "    <div class=\"col-2\"> \n" +
      "     <a href=\"http://freecode.com/projects/turnkey-twiki-appliance\"><img alt=\"Screenshot\" class=\"screenshotThumb\" src=\"http://a.fsdn.com/fm/images/no-screenshot.png?1386111726\"></a> \n" +
      "     <h2>Project Spotlight</h2> \n" +
      "     <h3><a href=\"http://freecode.com/projects/turnkey-twiki-appliance\">TurnKey TWiki Appliance</a></h3> \n" +
      "     <p>A TWiki appliance that is easy to use and lightweight.</p> \n" +
      "    </div> \n" +
      "   </div> \n" +
      "  </div> \n" +
      "  <div id=\"footer\"> \n" +
      "   <div class=\"footer-container\"> \n" +
      "    <div class=\"twitter-icon floatright\"> \n" +
      "     <a href=\"http://twitter.com/freecode_com\"><img src=\"http://twitter-badges.s3.amazonaws.com/follow_us-b.png\" alt=\"Follow Freecode on Twitter\"></a> \n" +
      "    </div> \n" +
      "    <div class=\"footerLinks\"> \n" +
      "     <a href=\"http://freecode.com/about\">About Freecode</a> \n" +
      "     <a target=\"_blank\" href=\"http://slashdotmedia.com/privacy-statement/\">Privacy</a> \n" +
      "     <a target=\"_blank\" href=\"http://slashdotmedia.com/opt-out-choices\">Cookies/Opt Out</a> \n" +
      "     <a target=\"_blank\" href=\"http://slashdotmedia.com/terms-of-use\">Terms</a> \n" +
      "     <a target=\"_blank\" href=\"http://slashdotmedia.com/\">Advertise</a> \n" +
      "     <!-- <a href=\"http://help.freecode.com/\" onclick=\"window.open(this.href);return false;\">Contact Us</a> --> \n" +
      "    </div> \n" +
      "    <div class=\"copyright\">\n" +
      "      © 2015 Slashdot Media. All Rights Reserved. Freecode is a \n" +
      "     <a target=\"_blank\" href=\"http://www.dhigroupinc.com/\">DHI service</a>. \n" +
      "    </div> \n" +
      "   </div> \n" +
      "  </div> \n" +
      "  <script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js\" type=\"text/javascript\" charset=\"utf-8\"></script> \n" +
      "  <script src=\"http://a.fsdn.com/fm/javascripts/base_packaged.js?1386111741\" type=\"text/javascript\"></script> \n" +
      "  <!-- Begin comScore Tag --> \n" +
      "  <script type=\"text/javascript\">\n" +
      "      document.write(unescape(\"%3Cscript src='\" +\n" +
      "      (document.location.protocol == \"https:\" ? \"https://sb\" : \"http://b\") +\n" +
      "      \".scorecardresearch.com/beacon.js' %3E%3C/script%3E\"));\n" +
      "    </script> \n" +
      "  <script type=\"text/javascript\">\n" +
      "      COMSCORE.beacon({\n" +
      "          c1:2,\n" +
      "          c2:6035546,\n" +
      "          c3:\"\",\n" +
      "          c4:\"\",\n" +
      "          c5:\"\",\n" +
      "          c6:\"\",\n" +
      "          c15:\"\"\n" +
      "      });\n" +
      "    </script> \n" +
      "  <noscript> \n" +
      "   <img src=\"http://b.scorecardresearch.com/p?c1=2&amp;c2=6035546&amp;c3=&amp;c4=&amp;c5=&amp;c6=&amp;c15=&amp;cj=1\" alt=\"comScore2\"> \n" +
      "  </noscript> \n" +
      "  <!-- End comScore Tag --> \n" +
      "  <script type=\"text/javascript\">document.write(unescape('%3Cscript type=\"text/javascript\" src=\"'+document.location.protocol+'//dnn506yrbagrg.cloudfront.net/pages/scripts/0011/0794.js\"%3E%3C%2Fscript%3E'))</script>   \n" +
      " </body>\n" +
      "</html>";
}
