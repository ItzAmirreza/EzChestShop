package tr.zeltuv.ezql.settings;

import com.zaxxer.hikari.HikariConfig;

public interface CustomHikariSettings {

    HikariConfig getHikariConfig(EzqlCredentials ezqlCredentials);
}
