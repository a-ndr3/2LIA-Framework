import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
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
      width: 450,
      color: AppColors.primaryColorBackground,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceAround,
        children: [
          MouseRegion(
              cursor: SystemMouseCursors.click,
              child: HoverableImageButton(
                  imageWidget: SvgPicture.asset(
                    'assets/icons/swagger.svg',
                    width: 50,
                    height: 50,
                  ),
                  onPressed: () =>
                      SupportMethods.openUrl(ApiEndpoints.swagger))),
          SizedBox(width: 30),
          MouseRegion(
              cursor: SystemMouseCursors.click,
              child: HoverableImageButton(
                  imageWidget: SvgPicture.asset(
                    'assets/icons/grafana1.svg',
                    width: 60,
                    height: 60,
                  ),
                  onPressed: () =>
                      SupportMethods.openUrl(ApiEndpoints.grafana))),
          SizedBox(width: 30),
          MouseRegion(
              cursor: SystemMouseCursors.click,
              child: HoverableImageButton(
                  imageWidget: SvgPicture.asset(
                    'assets/icons/prometheus.svg',
                    width: 50,
                    height: 50,
                  ),
                  onPressed: () =>
                      SupportMethods.openUrl(ApiEndpoints.prometheus))),
          SizedBox(width: 140),
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
