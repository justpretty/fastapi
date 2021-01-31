
import com.souher.sdk.database.DataModel;
import com.souher.sdk.iApp;

public class MiggrationProcess
{
    public static void main(String[] args) {
        try
        {
            initUIConfig();
        }
        catch (Exception e)
        {
            iApp.error(e);
        }
    }

    static void initUIConfig() throws Exception
    {

    }
}
