import 'package:flutter/material.dart';
import 'package:lss_thesis_ui/ApiEndpoints.dart';
import 'package:lss_thesis_ui/AppColors.dart';
import 'package:lss_thesis_ui/AppTextStyles.dart';
import 'package:lss_thesis_ui/Models/QueryAnalysisMetadata.dart';
import 'package:lss_thesis_ui/Models/QueryMetadata.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

class QueryAnalysisResultDialog extends StatelessWidget {
  final List<QueryAnalysisMetadata> queries;
  final VoidCallback onTableRefresh;

  QueryAnalysisResultDialog(
      {super.key, required this.queries, required this.onTableRefresh});

  final ScrollController _verticalScroll = ScrollController();
  final ScrollController _horizontalScroll = ScrollController();

  @override
  Widget build(BuildContext context) {
    return Dialog(
      insetPadding: const EdgeInsets.all(12),
      backgroundColor: AppColors.primaryColorBackground,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(25)),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            decoration: const BoxDecoration(
              color: Colors.transparent,
              borderRadius:
                  const BorderRadius.vertical(top: Radius.circular(25)),
            ),
            child: Row(
              children: [
                Text('Query Results (${queries.length})',
                    style: AppTextStyles.pageQueriesTitles
                        .copyWith(color: AppColors.primaryColorButton)),
                const Spacer(),
                IconButton(
                  icon: const Icon(Icons.close,
                      color: AppColors.primaryColorButton),
                  onPressed: () => Navigator.of(context).pop(),
                ),
              ],
            ),
          ),
          Container(
            constraints: const BoxConstraints(maxHeight: 700, maxWidth: 1800),
            //padding: const EdgeInsets.all(1),
            child: Scrollbar(
              controller: _verticalScroll,
              thickness: 8.0,
              trackVisibility: true,
              thumbVisibility: true,
              child: SingleChildScrollView(
                controller: _verticalScroll,
                scrollDirection: Axis.vertical,
                child: Scrollbar(
                  controller: _horizontalScroll,
                  thickness: 8.0,
                  trackVisibility: true,
                  thumbVisibility: true,
                  notificationPredicate: (notif) => notif.depth == 1,
                  child: SingleChildScrollView(
                    controller: _horizontalScroll,
                    scrollDirection: Axis.horizontal,
                    child: DataTable(
                      columns: [
                        DataColumn(
                            label: Flexible(
                                child: Center(
                                    child: Text('NAME',
                                        style: AppTextStyles.pageQueriesTitles
                                            .copyWith(
                                                color: AppColors
                                                    .primaryColorButton))))),
                        DataColumn(
                            label: Flexible(
                                child: Center(
                                    child: Text('QUERY',
                                        style: AppTextStyles.pageQueriesTitles
                                            .copyWith(
                                                color: AppColors
                                                    .primaryColorButton))))),
                        DataColumn(
                            label: Flexible(
                                child: Center(
                                    child: Text('CATEGORY',
                                        style: AppTextStyles.pageQueriesTitles
                                            .copyWith(
                                                color: AppColors
                                                    .primaryColorButton))))),
                        DataColumn(
                            label: Flexible(
                                child: Center(
                                    child: Text('TOPOLOGY CHECK',
                                        style: AppTextStyles.pageQueriesTitles
                                            .copyWith(
                                                color: AppColors
                                                    .primaryColorButton))))),
                        DataColumn(
                            label: Flexible(
                                child: Center(
                                    child: Text('STATUS',
                                        style: AppTextStyles.pageQueriesTitles
                                            .copyWith(
                                                color: AppColors
                                                    .primaryColorButton))))),
                      ],
                      rows: queries.map((q) {
                        return DataRow(cells: [
                          DataCell(Flexible(
                              child: Center(
                                  child: Text(_extractName(q.query),
                                      style: AppTextStyles.consoleText)))),
                          DataCell(
                            MouseRegion(
                              cursor: SystemMouseCursors.click,
                              child: GestureDetector(
                                onTap: () {
                                  final TextEditingController _editController =
                                      TextEditingController(text: q.query);
                                  showDialog(
                                    context: context,
                                    builder: (_) {
                                      return StatefulBuilder(
                                        builder: (context, setState) {
                                          return AlertDialog(
                                            backgroundColor: AppColors
                                                .primaryColorBackground,
                                            content: SizedBox(
                                              width: 600,
                                              child: Column(
                                                mainAxisSize: MainAxisSize.min,
                                                crossAxisAlignment:
                                                    CrossAxisAlignment.stretch,
                                                children: [
                                                  const Text("Edit Query",
                                                      style: AppTextStyles
                                                          .pageQueriesTitles),
                                                  const SizedBox(height: 10),
                                                  TextFormField(
                                                    controller: _editController,
                                                    maxLines: 8,
                                                    style: AppTextStyles
                                                        .consoleText,
                                                    decoration: InputDecoration(
                                                      filled: true,
                                                      fillColor: Colors.black12,
                                                      border:
                                                          OutlineInputBorder(),
                                                    ),
                                                  ),
                                                ],
                                              ),
                                            ),
                                            actions: [
                                              TextButton(
                                                onPressed: () async {
                                                  final updatedQuery =
                                                      q.copyWith(
                                                          query: _editController
                                                              .text);

                                                  final response =
                                                      await http.post(
                                                    Uri.parse(ApiEndpoints
                                                        .changeAnalysisQuery),
                                                    headers: {
                                                      "Content-Type":
                                                          "application/json"
                                                    },
                                                    body: jsonEncode(
                                                        updatedQuery.toJson()),
                                                  );

                                                  if (response.statusCode ==
                                                      200) {
                                                    Navigator.pop(context);
                                                    Navigator.pop(context);
                                                    onTableRefresh();
                                                    ScaffoldMessenger.of(
                                                            context)
                                                        .showSnackBar(
                                                      SnackBar(
                                                          content: Text(
                                                        "✅ Query updated!",
                                                        style: AppTextStyles
                                                            .snackBarText
                                                            .copyWith(
                                                                color: AppColors
                                                                    .consoleColorOutputSuccess),
                                                      )),
                                                    );
                                                  } else {
                                                    ScaffoldMessenger.of(
                                                            context)
                                                        .showSnackBar(
                                                      SnackBar(
                                                          content: Text(
                                                              "❌ Error: ${response.body}",
                                                              style: AppTextStyles
                                                                  .snackBarText
                                                                  .copyWith(
                                                                      color: AppColors
                                                                          .consoleColorOutputError))),
                                                    );
                                                  }
                                                },
                                                child: const Text("SAVE",
                                                    style: AppTextStyles
                                                        .buttonText),
                                              ),
                                              TextButton(
                                                onPressed: () =>
                                                    Navigator.pop(context),
                                                child: const Text("CLOSE",
                                                    style: AppTextStyles
                                                        .buttonText),
                                              ),
                                              TextButton(
                                                onPressed: q.status
                                                    ? () async {
                                                        final result =
                                                            await http.post(
                                                          Uri.parse(ApiEndpoints
                                                              .undeploy),
                                                          headers: {
                                                            "Content-Type":
                                                                "application/json"
                                                          },
                                                          body: jsonEncode(
                                                              q.toJson()),
                                                        );

                                                        Navigator.pop(context);

                                                        if (result.statusCode ==
                                                            200) {
                                                          ScaffoldMessenger.of(
                                                                  context)
                                                              .showSnackBar(
                                                            SnackBar(
                                                                content: Text(
                                                              "✔ Query undeployed successfully!",
                                                              style: AppTextStyles
                                                                  .snackBarText
                                                                  .copyWith(
                                                                      color: AppColors
                                                                          .consoleColorOutputSuccess),
                                                            )),
                                                          );
                                                          Navigator.pop(
                                                              context);
                                                          onTableRefresh();
                                                        } else {
                                                          ScaffoldMessenger.of(
                                                                  context)
                                                              .showSnackBar(
                                                            SnackBar(
                                                                content: Text(
                                                                    "❌ Undeployment failed: ${result.body}",
                                                                    style: AppTextStyles
                                                                        .snackBarText
                                                                        .copyWith(
                                                                            color:
                                                                                AppColors.consoleColorOutputError))),
                                                          );
                                                        }
                                                      }
                                                    : () async {
                                                        final result =
                                                            await http.post(
                                                          Uri.parse(ApiEndpoints
                                                              .deployAnalysisQueryFromDB),
                                                          headers: {
                                                            "Content-Type":
                                                                "application/json"
                                                          },
                                                          body: jsonEncode(
                                                              q.toJson()),
                                                        );

                                                        Navigator.pop(context);

                                                        if (result.statusCode ==
                                                            200) {
                                                          ScaffoldMessenger.of(
                                                                  context)
                                                              .showSnackBar(
                                                            SnackBar(
                                                                content: Text(
                                                              "🚀 Query deployed successfully!",
                                                              style: AppTextStyles
                                                                  .snackBarText
                                                                  .copyWith(
                                                                      color: AppColors
                                                                          .consoleColorOutputSuccess),
                                                            )),
                                                          );
                                                          Navigator.pop(
                                                              context);
                                                          onTableRefresh();
                                                        } else {
                                                          ScaffoldMessenger.of(
                                                                  context)
                                                              .showSnackBar(
                                                            SnackBar(
                                                                content: Text(
                                                                    "❌ Deploy failed: ${result.body}",
                                                                    style: AppTextStyles
                                                                        .snackBarText
                                                                        .copyWith(
                                                                            color:
                                                                                AppColors.consoleColorOutputError))),
                                                          );
                                                        }
                                                      },
                                                child: q.status
                                                    ? const Text("UNDEPLOY",
                                                        style: AppTextStyles
                                                            .buttonText)
                                                    : const Text("RUN",
                                                        style: AppTextStyles
                                                            .buttonText),
                                              ),
                                            ],
                                          );
                                        },
                                      );
                                    },
                                  );
                                },
                                child: Text(
                                  q.query.length > 30
                                      ? '${q.query.substring(0, 20)}...'
                                      : q.query,
                                  style: AppTextStyles.consoleText,
                                ),
                              ),
                            ),
                          ),
                          DataCell(Flexible(
                              child: Center(
                                  child: Text(q.category,
                                      style: AppTextStyles.consoleText)))),
                          DataCell(
                            Flexible(
                                child: Center(
                                    child: Icon(
                              q.checkTopology
                                  ? Icons.check_circle
                                  : Icons.cancel,
                              color:
                                  q.checkTopology ? Colors.green : Colors.red,
                            ))),
                          ),
                          DataCell(
                            Flexible(
                                child: Center(
                                    child: Icon(
                              q.status ? Icons.check_circle : Icons.cancel,
                              color: q.status ? Colors.green : Colors.red,
                            ))),
                          ),
                        ]);
                      }).toList(),
                    ),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  String _extractName(String query) {
    final RegExp regExp = RegExp(r"@name\('(.+?)'\)");
    final Match? match = regExp.firstMatch(query);
    return match?.group(1) ??
        query.substring(0, query.length < 10 ? query.length : 10);
  }
}
