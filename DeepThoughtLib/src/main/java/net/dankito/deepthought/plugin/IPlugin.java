package net.dankito.deepthought.plugin;

/**
 * Created by ganymed on 17/07/15.
 */
public interface IPlugin {

  /**
   * <p>
   *   The DeepThough Plugin System Version this Plugin is build on.
   *   PluginManager than compares this value to Application.CurrentPluginSystemVersion to check if this Plugin is compatible with DeepThought version.
   * </p>
   * @return
   */
  int getSupportedPluginSystemVersion();

  /**
   * <p>
   *   The Plugin's name.
   *   Displayed to the User in the Plugin Settings Screen.
   * </p>
   * @return
   */
  String getName();

  /**
   * <p>
   *   The Plugin's Version.
   *   Not Relevant to the Plugin System itself.
   *   Displayed to the User in the Plugin Settings Screen.
   * </p>
   * @return
   */
  String getPluginVersion();

//  public void initialize();

}
