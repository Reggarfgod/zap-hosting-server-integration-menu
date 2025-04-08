package com.reggarf.mods.zap_hosting_server_integration_menu.config;




import com.reggarf.mods.zap_hosting_server_integration_menu.Zap_Hosting;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = Zap_Hosting.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/mossy_cobblestone.png")
public class ZHConfig extends PartitioningSerializer.GlobalData {

    @ConfigEntry.Category("common")
    @ConfigEntry.Gui.TransitiveObject()
    public Common common = new Common();

//    @ConfigEntry.Category("client")
//    @ConfigEntry.Gui.TransitiveObject()
//    public Client client = new Client();

    @Config(name = "common")
    public static final class Common implements ConfigData {

        @ConfigEntry.Gui.Tooltip
        @Comment("Enable or disable the welcome message")
        public Boolean enableOverlay = true;

        @ConfigEntry.Gui.Tooltip
        @Comment("zap hosting link")
        public String link = "https://zap-hosting.com/reggarf";

        @ConfigEntry.Gui.Tooltip
        @Comment("Zap Code")
        public String code = "REGGARF-1047";

        @ConfigEntry.Gui.Tooltip
        @Comment("Zap Code")
        public String serverIP = "REGGARF-1047";

    }

//    @Config(name = "client")
//    public static final class Client implements ConfigData {
//
//        @ConfigEntry.Gui.Tooltip
//        @Comment("models")
//        public boolean showFirstPerson = true;
//
//    }
}
