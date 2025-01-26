package com.espertech;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.client.fireandforget.EPFireAndForgetQueryResult;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.UUID;

@RestController
@RequestMapping("/queries")
public class EsperController {
    //private final EsperService esperService;

//    @Autowired
//    public EsperController(EsperService service) {
//        this.esperService = service;
//    }

    public EsperController() {

    }

//    @PostMapping("/add")
//    public String addQuery(@RequestBody String epl) {
//        try {
//            String tmp = String.format("@name('my-statement%s') %s;\n", UUID.randomUUID().getMostSignificantBits(), epl);
//            var compiled = EPCompilerProvider.getCompiler().compile(tmp, new CompilerArguments(Main.esperService.getConfiguration()));
//            EPDeployment deployment = Main.esperService.getRuntime().getDeploymentService().deploy(compiled); //todo make service that is responsible for creating correct sql queries
//            return "Query added: " + deployment.getDeploymentId();
//        } catch (Exception e) {
//            return "Failed to add query: " + e.getMessage();
//        }
//    }
//
//    @GetMapping("/getQueries")
//    public ResponseEntity<String> getEPLStatements() {
//        try {
//            var queries = Arrays.stream(Main.esperService.getDeployment().getStatements()).toList(); //todo get service injected
//            var sb = new StringBuilder();
//            for (var query : queries) {
//                sb.append(query.getName()).append("\n");
//            }
//            return ResponseEntity.ok().body(sb.toString());
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("Failed to get queries: " + e.getMessage());
//        }
//    }
//
//    @PostMapping("/onDemandQuery") //todo add sliding window ~ 2 mins
//    public ResponseEntity<String> onDemandQuery(@RequestBody String epl) {
//        try {
//            var runtime = Main.esperService.getRuntime();
//            CompilerArguments compilerArguments = new CompilerArguments(Main.esperService.getConfiguration());
//            compilerArguments.getPath().add(runtime.getRuntimePath());
//            var compiled = EPCompilerProvider.getCompiler().compileQuery(epl, compilerArguments);
//            EPFireAndForgetQueryResult result = runtime.getFireAndForgetService().executeQuery(compiled);
//            var sb = new StringBuilder();
//            for (EventBean row : result.getArray()) {
//                sb.append(row.getUnderlying()).append("\n");
//            }
//            return ResponseEntity.ok().body(sb.toString());
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("Failed to run query: " + e.getMessage());
//        }
//    }
}
