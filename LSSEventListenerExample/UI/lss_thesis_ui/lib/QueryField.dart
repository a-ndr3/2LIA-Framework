import 'package:flutter/material.dart';
import 'package:lss_thesis_ui/AppColors.dart';
import 'package:lss_thesis_ui/AppTextStyles.dart';
import 'package:lss_thesis_ui/SupportMethods.dart';
import 'package:lss_thesis_ui/buttons.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

class QueryField extends StatefulWidget {
  final Function(String) onSend;
  final VoidCallback? onExistingQueries;
  const QueryField({required this.onSend, this.onExistingQueries, Key? key})
      : super(key: key);

  @override
  _QueryInputState createState() => _QueryInputState();
}

class _QueryInputState extends State<QueryField> {
  final supportMethods = SupportMethods();

  TextEditingController _controller = TextEditingController();

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            HoverIconButton(
                icon: const Icon(FontAwesomeIcons.database),
                color: AppColors.primaryColorButton,
                onPressed: () {
                  widget.onExistingQueries!();
                }),
            SizedBox(width: 10),
            Container(
              width: 900,
              height: 250,
              child: Scrollbar(
                child: TextField(
                  controller: _controller,
                  maxLines: null,
                  expands: true,
                  textAlignVertical: TextAlignVertical.top,
                  style: AppTextStyles.consoleText,
                  decoration: const InputDecoration(
                    filled: true,
                    fillColor: Colors.black45,
                    border: OutlineInputBorder(),
                    contentPadding: EdgeInsets.all(8),
                  ),
                ),
              ),
            ),
          ],
        ),
        SizedBox(height: 10),
        HoverableElevatedButton(
            child: const Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text('SEND', style: AppTextStyles.buttonText),
              ],
            ),
            onPressed: () =>
                {widget.onSend(_controller.text), _controller.clear()}),
      ],
    );
  }
}
