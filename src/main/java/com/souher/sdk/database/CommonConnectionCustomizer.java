package com.souher.sdk.database;

import com.mchange.v2.c3p0.AbstractConnectionCustomizer;

import java.sql.Connection;
import java.sql.Statement;

public class CommonConnectionCustomizer extends AbstractConnectionCustomizer {
    @Override
    public void onAcquire(Connection c, String parentDataSourceIdentityToken) throws Exception {
        super.onAcquire(c, parentDataSourceIdentityToken);
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            stmt.executeUpdate("SET names utf8mb4");
        }
        finally
        {
            if (stmt != null) stmt.close();
        }
    }

}
