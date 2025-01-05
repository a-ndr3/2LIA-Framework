import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.DeploymentOptions;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.runtime.client.EPRuntime;

public class TemperatureEsperQueries {
    public static EPCompiled compileEPL(Configuration conf) {
        var context = "@public create context TemperatureContext initiated by Temperature as t terminated by " +
                "Temperature(location = t.location and temperature = t.temperature);\n";

        var tempAlert = "@name('alert') context TemperatureContext select * from Temperature where " +
                "(temperature < -45 or temperature > 45) and (location = 'A' or location = 'J');\n";

//        var insertIntoObserved = "context TemperatureContext insert into ObservedTemperature select location, temperature from Temperature" +
//                " where (temperature < -35 or temperature > 35) or (location = 'A' or location = 'J');\n";
//
//        var tempAlert = "@name('alert') context TemperatureContext select * from pattern [" +
//                "observed=ObservedTemperature(temperature = context.t.temperature and location = context.t.location)" +
//                "-> every t=Temperature(location != observed.location and temperature != observed.temperature)];\n";

//        var tempAlert = "@name('alert') context TemperatureContext select * from pattern [" +
//                "observed=Temperature(temperature < -30 or temperature > 25 or location = 'A' or location = 'J')" +
//                "-> every t=Temperature(temperature != observed.temperature and location != observed.location)];\n";

        EPCompiled compiled;
        try {
            compiled = EPCompilerProvider.getCompiler().compile(context + /*insertIntoObserved +*/ tempAlert, new CompilerArguments(conf));
        } catch (EPCompileException ex) {
            throw new RuntimeException(ex);
        }

        return compiled;
    }

    public static Configuration getConfiguration() {
        Configuration configuration = new Configuration();
        configuration.getCommon().addEventType(Temperature.class);
        return configuration;
    }

    public static void deploy(EPRuntime runtime, EPCompiled compiled) {
        try {
            runtime.getDeploymentService().deploy(compiled, new DeploymentOptions().setDeploymentId("temperatureQueries"));
        } catch (EPDeployException ex) {
            throw new RuntimeException(ex);
        }
    }
}
