import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:lss_thesis_ui/AppColors.dart';
import 'package:lss_thesis_ui/SupportMethods.dart';
import 'package:flutter_svg/flutter_svg.dart';

class HoverIconButton extends StatefulWidget {
  final Icon icon;
  final VoidCallback? onPressed;
  final double scale;
  final Color color;

  const HoverIconButton({
    Key? key,
    required this.icon,
    this.onPressed,
    this.scale = 1.2,
    required this.color,
  }) : super(key: key);

  @override
  _HoverIconButtonState createState() => _HoverIconButtonState();
}

class _HoverIconButtonState extends State<HoverIconButton> {
  bool isHovered = false;

  @override
  Widget build(BuildContext context) {
    final SupportMethods supportMethods = SupportMethods();
    return MouseRegion(
      onEnter: (_) => _setHovered(true),
      onExit: (_) => _setHovered(false),
      cursor: SystemMouseCursors.click,
      child: GestureDetector(
        onTap: widget.onPressed,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          transform: Matrix4.identity()..scale(isHovered ? widget.scale : 1.0),
          transformAlignment: Alignment.center,
          child: Icon(
            color: widget.color,
            widget.icon.icon,
            size: supportMethods.getCorrectSize(context, 65.sp, 25.sp, 25),
          ),
        ),
      ),
    );
  }

  void _setHovered(bool value) {
    setState(() {
      isHovered = value;
    });
  }
}

class HoverableElevatedButton extends StatefulWidget {
  final Widget child;
  final VoidCallback onPressed;

  HoverableElevatedButton({required this.child, required this.onPressed});

  @override
  _HoverableElevatedButtonState createState() =>
      _HoverableElevatedButtonState();
}

class _HoverableElevatedButtonState extends State<HoverableElevatedButton> {
  bool _isHovering = false;

  @override
  Widget build(BuildContext context) {
    final buttonStyle = ElevatedButton.styleFrom(
      backgroundColor: _isHovering ? Colors.transparent : Colors.transparent,
      shadowColor: Colors.transparent,
      surfaceTintColor: Colors.transparent,
      foregroundColor: Colors.transparent,
      padding: const EdgeInsets.symmetric(horizontal: 30, vertical: 20),
      side: const BorderSide(color: AppColors.primaryColorButton, width: 2),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(35),
      ),
      elevation: _isHovering ? 3 : 0,
    );

    return MouseRegion(
      onEnter: (_) => _setHovering(true),
      onExit: (_) => _setHovering(false),
      child: Transform.scale(
        scale: _isHovering ? 1.03 : 1,
        alignment: Alignment.center,
        child: ElevatedButton(
          style: buttonStyle,
          onPressed: widget.onPressed,
          child: FractionalTranslation(
            translation: _isHovering ? Offset(-0.005, -0.005) : Offset.zero,
            child: widget.child,
          ),
        ),
      ),
    );
  }

  void _setHovering(bool isHovering) {
    setState(() {
      _isHovering = isHovering;
    });
  }
}

class HoverableTextButton extends StatefulWidget {
  final Widget child;
  final VoidCallback onPressed;

  HoverableTextButton({required this.child, required this.onPressed});

  @override
  _HoverableTextButtonState createState() => _HoverableTextButtonState();
}

class _HoverableTextButtonState extends State<HoverableTextButton> {
  bool _isHovering = false;

  @override
  Widget build(BuildContext context) {
    final buttonStyle = TextButton.styleFrom(
      backgroundColor: _isHovering
          ? AppColors.primaryColorBackground
          : AppColors.primaryColorBackground,
      shadowColor: Colors.transparent,
      surfaceTintColor: AppColors.primaryColorBackground,
      foregroundColor: AppColors.primaryColorBackground,
      padding: const EdgeInsets.symmetric(horizontal: 30, vertical: 20),
    );

    return MouseRegion(
      onEnter: (_) => _setHovering(true),
      onExit: (_) => _setHovering(false),
      child: Transform.scale(
        scale: _isHovering ? 1.15 : 1,
        alignment: Alignment.center,
        child: TextButton(
          style: buttonStyle,
          onPressed: widget.onPressed,
          child: FractionalTranslation(
            translation: _isHovering ? Offset(-0.010, -0.005) : Offset.zero,
            child: widget.child,
          ),
        ),
      ),
    );
  }

  void _setHovering(bool isHovering) {
    setState(() {
      _isHovering = isHovering;
    });
  }
}

class HoverableImageButton extends StatefulWidget {
  final Widget imageWidget;
  final VoidCallback onPressed;

  const HoverableImageButton(
      {required this.imageWidget, required this.onPressed, Key? key})
      : super(key: key);

  @override
  _HoverableImageButtonState createState() => _HoverableImageButtonState();
}

class _HoverableImageButtonState extends State<HoverableImageButton> {
  bool _isHovering = false;

  @override
  Widget build(BuildContext context) {
    return MouseRegion(
      onEnter: (_) => _setHovering(true),
      onExit: (_) => _setHovering(false),
      child: Transform.scale(
        scale: _isHovering ? 1.15 : 1,
        alignment: Alignment.center,
        child: GestureDetector(
          onTap: widget.onPressed,
          child: widget.imageWidget,
        ),
      ),
    );
  }

  void _setHovering(bool isHovering) {
    setState(() {
      _isHovering = isHovering;
    });
  }
}

class HoverableIcon extends StatefulWidget {
  final Icon icon;
  final double size;

  const HoverableIcon({
    Key? key,
    required this.icon,
    this.size = 24.0,
  }) : super(key: key);

  @override
  _HoverableIconState createState() => _HoverableIconState();
}

class _HoverableIconState extends State<HoverableIcon> {
  bool _isHovering = false;

  @override
  Widget build(BuildContext context) {
    return MouseRegion(
      onEnter: (_) => _setHovering(true),
      onExit: (_) => _setHovering(false),
      child: Transform.scale(
        scale: _isHovering ? 1.20 : 1,
        alignment: Alignment.center,
        child: widget.icon,
      ),
    );
  }

  void _setHovering(bool isHovering) {
    setState(() {
      _isHovering = isHovering;
    });
  }
}
