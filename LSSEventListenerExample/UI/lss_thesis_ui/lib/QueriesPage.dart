import 'package:flutter/material.dart';
import 'package:lss_thesis_ui/ApiEndpoints.dart';
import 'package:lss_thesis_ui/AppColors.dart';
import 'package:lss_thesis_ui/AppTextStyles.dart';
import 'package:lss_thesis_ui/Models/QueryAnalysisMetadata.dart';
import 'package:lss_thesis_ui/Models/QueryMetadata.dart';
import 'package:lss_thesis_ui/QueryAnalysisResultDialog.dart';
import 'package:lss_thesis_ui/QueryField.dart';
import 'package:lss_thesis_ui/QueryResultsDialog.dart';
import 'package:lss_thesis_ui/SupportMethods.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

import 'package:lss_thesis_ui/buttons.dart';

class QueryPage extends StatefulWidget {
  @override
  _QueryPageState createState() => _QueryPageState();
}

class _QueryPageState extends State<QueryPage> {
  bool _isLoading = false;
  List<String> _consoleOutput = [];
  List<String> _tempOutput = [];
  List<QueryMetadata> _queriesForTable = [];
  List<QueryAnalysisMetadata> _queriesForAnalysisTable = [];
  bool _showTable = false;

  bool _isConsoleVisible = false;

  SupportMethods supportMethods = SupportMethods();

  Future<void> _sendQuery(String query) async {
    if (query.isEmpty) {
      addMsgToConsole('Query cannot be empty.');
      return;
    }
    addMsgToConsole('Sent Query: $query');

    final response = await http.post(
      Uri.parse(ApiEndpoints.onDemand),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'query': query}),
    );

    if (response.statusCode == 200) {
      addMsgToConsole('Response: ${response.body}');
    } else {
      addMsgToConsole('Error: ${response.statusCode} - ${response.body}');
    }
  }

  Future<void> _sendAnalysisQuery(String query) async {
    if (query.isEmpty) {
      addMsgToConsole('Query cannot be empty.');
      return;
    }
    addMsgToConsole('Sent Query: $query');

    final response = await http.post(
      Uri.parse(ApiEndpoints.addAnalysisQuery),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'query': query}),
    );

    if (response.statusCode == 200) {
      addMsgToConsole('Response: ${response.body}');
    } else {
      addMsgToConsole('Error: ${response.statusCode} - ${response.body}');
    }
  }

  Future<List<QueryMetadata>> _existingQueriesAggregation() async {
    try {
      final response = await http.post(
        Uri.parse(ApiEndpoints.checkExistingQueriesInDatabase),
      );

      if (response.statusCode == 200) {
        if (response.body.isNotEmpty) {
          final List<dynamic> data = jsonDecode(response.body);
          addMsgToConsole('Found ${data.length} queries.');
          return data.map((json) => QueryMetadata.fromJson(json)).toList();
        } else {
          addMsgToConsole('No queries found.');
          return [];
        }
      } else {
        addMsgToConsole('Error: ${response.statusCode} - ${response.body}');
        return [];
      }
    } catch (e) {
      addMsgToConsole('Error: ${e}');
      return [];
    }
  }

  Future<void> _refreshAndShowQueryTable(
      Future<List<QueryMetadata>> Function() fetcher) async {
    setState(() => _isLoading = true);

    final result = await fetcher();

    setState(() {
      _queriesForTable = result;
      _queriesForTable.sort((a, b) {
        if (a.status != b.status) return a.status ? 1 : -1;
        return b.updatedAt.compareTo(a.updatedAt);
      });
      _showTable = _queriesForTable.isNotEmpty;
      _isLoading = false;
    });

    if (_showTable) {
      showDialog(
        context: context,
        builder: (_) => QueryResultsDialog(
          queries: _queriesForTable,
          onTableRefresh: () => _refreshAndShowQueryTable(fetcher),
        ),
      );
    }
  }

  Future<void> _refreshAndShowAnalysisQueryTable(
      Future<List<QueryAnalysisMetadata>> Function() fetcher) async {
    setState(() => _isLoading = true);

    final result = await fetcher();

    setState(() {
      _queriesForAnalysisTable = result;
      _showTable = _queriesForAnalysisTable.isNotEmpty;
      _isLoading = false;
    });

    if (_showTable) {
      showDialog(
        context: context,
        builder: (_) => QueryAnalysisResultDialog(
          queries: _queriesForAnalysisTable,
          onTableRefresh: () => _refreshAndShowAnalysisQueryTable(fetcher),
        ),
      );
    }
  }

  Future<List<QueryAnalysisMetadata>> _existingQueriesAnalytics() async {
    try {
      final response = await http.post(
        Uri.parse(ApiEndpoints.checkExistingAnalysisQueriesInDatabase),
      );

      if (response.statusCode == 200) {
        if (response.body.isNotEmpty) {
          final List<dynamic> data = jsonDecode(response.body);
          addMsgToConsole('Found ${data.length} queries.');
          return data
              .map((json) => QueryAnalysisMetadata.fromJson(json))
              .toList();
        } else {
          addMsgToConsole('No queries found.');
          return [];
        }
      } else {
        addMsgToConsole('Error: ${response.statusCode} - ${response.body}');
        return [];
      }
    } catch (e) {
      addMsgToConsole('Error: ${e}');
      return [];
    }
  }

  void addMsgToConsole(String msg) {
    if (!_isConsoleVisible) {
      _tempOutput.add(msg);
    } else if (_isConsoleVisible) {
      _consoleOutput.add(msg);
      setState(() {});
    }
  }

  void _toggleConsole() {
    if (_isConsoleVisible) {
      _tempOutput.addAll(_consoleOutput);
      setState(() {
        _isConsoleVisible = !_isConsoleVisible;
      });
      _consoleOutput.clear();
    } else if (!_isConsoleVisible) {
      _consoleOutput.addAll(_tempOutput);
      setState(() {
        _isConsoleVisible = !_isConsoleVisible;
      });
      _tempOutput.clear();
    }
  }

  void _clearConsole() {
    _consoleOutput.clear();
    _tempOutput.clear();
    setState(() {
      _isConsoleVisible = true;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        Scaffold(
          backgroundColor: AppColors.primaryColorBackground,
          body: Row(
            children: [
              Expanded(
                flex: 2,
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: SingleChildScrollView(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        SizedBox(height: 80),
                        const Text("AGGREGATION QUERIES",
                            style: AppTextStyles.pageQueriesTitles),
                        SizedBox(height: 10),
                        QueryField(
                          onSend: _sendQuery,
                          onExistingQueries: () => _refreshAndShowQueryTable(
                              _existingQueriesAggregation),
                          // () async {
                          //   final result = await _existingQueriesAggregation();
                          //   setState(() {
                          //     _queriesForTable = result;
                          //     _queriesForTable.sort((a, b) {
                          //       return b.updatedAt.compareTo(a.updatedAt);
                          //     });
                          //     _showTable = _queriesForTable.isNotEmpty;
                          //     showDialog(
                          //       context: context,
                          //       builder: (_) =>
                          //           QueryResultsDialog(queries: _queriesForTable),
                          //     );
                          //   });
                          // },
                        ),
                        SizedBox(height: 40),
                        const Text("ANALYTICS QUERIES",
                            style: AppTextStyles.pageQueriesTitles),
                        SizedBox(height: 10),
                        QueryField(
                          onSend: _sendAnalysisQuery,
                          onExistingQueries: () =>
                              _refreshAndShowAnalysisQueryTable(
                                  _existingQueriesAnalytics),
                          // () async {
                          //   final result = await _existingQueriesAnalytics();
                          //   setState(() {
                          //     _queriesForTable = result;
                          //     _showTable = _queriesForTable.isNotEmpty;
                          //     showDialog(
                          //       context: context,
                          //       builder: (_) =>
                          //           QueryResultsDialog(queries: _queriesForTable),
                          //     );
                          //   });
                          // },
                        ),
                        const SizedBox(height: 200),
                        Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              SizedBox(width: 150),
                              supportMethods.menuContainer(context)
                            ]),
                      ],
                    ),
                  ),
                ),
              ),
              Stack(
                alignment: Alignment.centerRight,
                children: [
                  AnimatedContainer(
                    duration: Duration(milliseconds: 370),
                    width: _isConsoleVisible ? 450 : 40,
                    color: Colors.black54,
                    padding: EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: [
                        _isConsoleVisible
                            ? Align(
                                alignment: Alignment.topLeft,
                                child: Column(children: [
                                  MouseRegion(
                                    cursor: SystemMouseCursors.click,
                                    child: GestureDetector(
                                      onTap: _toggleConsole,
                                      child: const HoverableIcon(
                                          icon: Icon(Icons.arrow_forward_ios,
                                              color: AppColors
                                                  .primaryColorButton)),
                                    ),
                                  ),
                                  const SizedBox(height: 20),
                                  MouseRegion(
                                    cursor: SystemMouseCursors.click,
                                    child: GestureDetector(
                                      onTap: _clearConsole,
                                      child: const HoverableIcon(
                                          icon: Icon(Icons.clear,
                                              color: AppColors
                                                  .primaryColorButton)),
                                    ),
                                  ),
                                  const SizedBox(height: 20),
                                ]))
                            : Align(
                                alignment: Alignment.topLeft,
                                child: MouseRegion(
                                    cursor: SystemMouseCursors.click,
                                    child: GestureDetector(
                                      onTap: _toggleConsole,
                                      child: const HoverableIcon(
                                          icon: Icon(Icons.arrow_back_ios,
                                              color: AppColors
                                                  .primaryColorButton)),
                                    ))),
                        Expanded(
                          child: ListView(
                            children: _consoleOutput
                                .map((e) => Text(e,
                                    style: const TextStyle(
                                        color: AppColors.consoleColorOutput)))
                                .toList(),
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
        if (_isLoading)
          Container(
            color: Colors.black.withOpacity(0.4),
            child: const Center(
              child: CircularProgressIndicator(
                  backgroundColor: AppColors.primaryColorButton),
            ),
          ),
      ],
    );
  }
}
