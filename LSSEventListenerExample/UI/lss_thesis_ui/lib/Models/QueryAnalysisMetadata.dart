class QueryAnalysisMetadata {
  final String query;
  final String category;
  final bool status;
  final bool checkTopology;

  QueryAnalysisMetadata({
    required this.query,
    required this.category,
    required this.checkTopology,
    required this.status,
  });

  QueryAnalysisMetadata copyWith({
    String? query,
  }) {
    return QueryAnalysisMetadata(
      query: query ?? this.query,
      category: category,
      checkTopology: checkTopology,
      status: status,
    );
  }

  Map<String, dynamic> toJson() => {
        'query': query,
        'category': category,
        'description': checkTopology,
        'status': status,
      };

  factory QueryAnalysisMetadata.fromJson(Map<String, dynamic> json) {
    return QueryAnalysisMetadata(
      query: json['query'],
      category: json['category'],
      checkTopology: json['description'] == '1' ? true : false,
      status: json['status'],
    );
  }
}
