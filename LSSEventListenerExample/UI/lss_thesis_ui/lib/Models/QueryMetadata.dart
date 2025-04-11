class QueryMetadata {
  final String id;
  final String name;
  final String queryStatement;
  final String deploymentId;
  final String query;
  final String eventClasses;
  final String category;
  final DateTime createdAt;
  final DateTime updatedAt;
  final bool status;
  final String description;

  QueryMetadata({
    required this.id,
    required this.name,
    required this.queryStatement,
    required this.deploymentId,
    required this.query,
    required this.eventClasses,
    required this.category,
    required this.createdAt,
    required this.updatedAt,
    required this.status,
    required this.description,
  });

  QueryMetadata copyWith({
    String? query,
  }) {
    return QueryMetadata(
      id: id,
      name: name,
      queryStatement: queryStatement,
      deploymentId: deploymentId,
      query: query ?? this.query,
      eventClasses: eventClasses,
      category: category,
      createdAt: createdAt,
      updatedAt: updatedAt,
      status: status,
      description: description,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'queryStatement': queryStatement,
        'deploymentId': deploymentId,
        'query': query,
        'eventClasses': eventClasses.split(',').map((e) => e.trim()).toList(),
        'category': category,
        'createdAt': createdAt.millisecondsSinceEpoch,
        'updatedAt': updatedAt.millisecondsSinceEpoch,
        'status': status,
        'description': description,
      };

  factory QueryMetadata.fromJson(Map<String, dynamic> json) {
    return QueryMetadata(
      id: json['id'],
      name: json['name'],
      queryStatement: json['queryStatement'],
      deploymentId: json['deploymentId'],
      query: json['query'],
      eventClasses: (json['eventClasses'] as List<dynamic>).join(', '),
      category: json['category'],
      createdAt: DateTime.fromMillisecondsSinceEpoch(json['createdAt']),
      updatedAt: DateTime.fromMillisecondsSinceEpoch(json['updatedAt']),
      status: json['status'],
      description: json['description'],
    );
  }
}
