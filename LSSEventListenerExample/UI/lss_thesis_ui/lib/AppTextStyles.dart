import 'package:flutter/material.dart';
import 'AppColors.dart';

class AppTextStyles {
  static const TextStyle pageMenuText = TextStyle(
    fontFamily: 'IBM Plex Mono',
    fontSize: 24,
    fontWeight: FontWeight.w400,
    color: AppColors.primaryColorText,
  );

  static const TextStyle pageMenuTexthighlighted = TextStyle(
    fontFamily: 'IBM Plex Mono',
    fontSize: 28,
    fontWeight: FontWeight.w800,
    color: AppColors.primaryColorHighlight,
  );

  static const TextStyle pageQueriesTitles = TextStyle(
    fontFamily: 'IBM Plex Sans',
    fontSize: 18,
    fontWeight: FontWeight.w400,
    color: AppColors.primaryColorText,
  );

  static const TextStyle buttonText = TextStyle(
    fontFamily: 'IBM Plex Sans',
    fontSize: 14,
    fontWeight: FontWeight.w600,
    color: AppColors.primaryColorButton,
  );

  static const TextStyle consoleText = TextStyle(
    fontFamily: 'Inconsolata',
    fontSize: 14,
    fontWeight: FontWeight.w500,
    color: AppColors.consoleColorOutput,
  );

  static const TextStyle snackBarText = TextStyle(
    fontFamily: 'Roboto',
    fontSize: 14,
    fontWeight: FontWeight.w800,
    color: AppColors.consoleColorOutput,
  );
}
