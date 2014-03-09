/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.ctts;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * カラーチーミングチームサイン
 * @author ucchy
 */
public class ColorTeamingTeamSign extends JavaPlugin {

    /**
     * プラグインが有効化されたときに呼ばれるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        // ColorTeamingの取得、dependに指定しているので必ず取得できる。
        Plugin colorteaming = getServer().getPluginManager().getPlugin("ColorTeaming");
        String ctversion = colorteaming.getDescription().getVersion();
        if ( !isUpperVersion(ctversion, "2.3.0") ) {
            getLogger().warning("ColorTeaming のバージョンが古いため、ColorTeamingTeamSign が使用できません。");
            getLogger().warning("ColorTeaming v2.3.0 以降のバージョンをご利用ください。");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // ブリッヂの作成
        ColorTeamingBridge bridge = new ColorTeamingBridge(colorteaming);
        
        // リスナーの登録
        getServer().getPluginManager().registerEvents(new TeamSignListener(bridge), this);
    }

    /**
     * 指定されたバージョンが、基準より新しいバージョンかどうかを確認する
     * @param version 確認するバージョン
     * @param border 基準のバージョン
     * @return 基準より確認対象の方が新しいバージョンかどうか<br/>
     * ただし、無効なバージョン番号（数値でないなど）が指定された場合はfalseに、
     * 2つのバージョンが完全一致した場合はtrueになる。
     */
    private static boolean isUpperVersion(String version, String border) {

        String[] versionArray = version.split("\\.");
        int[] versionNumbers = new int[versionArray.length];
        for ( int i=0; i<versionArray.length; i++ ) {
            if ( !versionArray[i].matches("[0-9]+") )
                return false;
            versionNumbers[i] = Integer.parseInt(versionArray[i]);
        }

        String[] borderArray = border.split("\\.");
        int[] borderNumbers = new int[borderArray.length];
        for ( int i=0; i<borderArray.length; i++ ) {
            if ( !borderArray[i].matches("[0-9]+") )
                return false;
            borderNumbers[i] = Integer.parseInt(borderArray[i]);
        }

        int index = 0;
        while ( (versionNumbers.length > index) && (borderNumbers.length > index) ) {
            if ( versionNumbers[index] > borderNumbers[index] ) {
                return true;
            } else if ( versionNumbers[index] < borderNumbers[index] ) {
                return false;
            }
            index++;
        }
        if ( borderNumbers.length == index ) {
            return true;
        } else {
            return false;
        }
    }
}
