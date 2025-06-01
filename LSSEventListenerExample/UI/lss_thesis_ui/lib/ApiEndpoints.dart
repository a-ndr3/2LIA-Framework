class ApiEndpoints {
  static const String baseUrl = "http://127.0.0.1:8081";
  static const String queries = "$baseUrl/queries";
  static const String onDemand = "$queries/onDemandQuery";

  static const String addAnalysisQuery = "$queries/addAnalysisQuery";
  static const String addQueryAuto = "$queries/addQueryAuto";

  static const String checkExistingQueriesInDatabase =
      "$queries/checkExistingQueriesInDatabase";
  static const String checkExistingAnalysisQueriesInDatabase =
      "$queries/checkExistingAnalysisQueriesInDatabase";

  static const String changeQuery = "$queries/changeQuery";
  static const String changeAnalysisQuery = "$queries/changeAnalysisQuery";

  static const String deployAnalysisQueryFromDB =
      "$queries/deployAnalysisQueryFromDB";
  static const String deployQueryFromDB = "$queries/deployQueryFromDB";

  static const String undeploy = "$queries/undeployQuery";

  static const String pingDB = "$queries/pingDB";

  static const String grafana = "http://127.0.0.1:3000/login";

  static const String prometheus = "http://127.0.0.1:9308/metrics";

  static const String swagger = "http://127.0.0.1:8081/swagger-ui/index.html";
}
