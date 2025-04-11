import 'package:flutter/material.dart';
import 'package:lss_thesis_ui/AppColors.dart';
import 'package:lss_thesis_ui/AppTextStyles.dart';
import 'package:url_launcher/url_launcher.dart';
import 'ApiEndpoints.dart';
import 'buttons.dart';

class SupportMethods {
  static void openUrl(String url) async {
    final Uri uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(
        uri,
        mode: LaunchMode.externalApplication,
      );
    } else {
      throw 'Could not launch $url';
    }
  }

  bool isMobile(context) {
    final screenWidth = MediaQuery.of(context).size.width;
    final screenHeight = MediaQuery.of(context).size.height;
    return screenWidth < 600 || (screenWidth < 1080 && screenHeight < 600);
  }

  double getCorrectSize(
      context, double mobileSize, double mobileSideSize, double desktopSize) {
    if (isMobile(context) && !isMobileWidth(context)) {
      return mobileSideSize;
    }
    if (isMobile(context) && isMobileWidth(context)) {
      return mobileSize;
    }
    return desktopSize;
  }

  bool isMobileWidth(context) {
    final screenWidth = MediaQuery.of(context).size.width;
    return screenWidth < 600;
  }

  Container menuContainer(context) {
    return Container(
      width: 460,
      color: AppColors.primaryColorBackground,
      child: Row(
        children: [
          HoverableTextButton(
            child: const Text('QUERIES', style: AppTextStyles.pageMenuText),
            onPressed: () => Navigator.pushNamed(context, '/'),
          ),
          HoverableTextButton(
            child: const Text('GRAFANA', style: AppTextStyles.pageMenuText),
            onPressed: () => SupportMethods.openUrl(ApiEndpoints.grafana),
          )
        ],
      ),
    );
  }

  Table getExistingQueries(context, type) {
    if (type == 'aggregation') {
      return Table();
    } else {
      return Table();
    }
  }
}
