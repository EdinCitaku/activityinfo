/**
 * Sencha GXT 4.0.0 - Sencha for GWT
 * Copyright (c) 2006-2015, Sencha Inc.
 *
 * licensing@sencha.com
 * http://www.sencha.com/products/gxt/license/
 *
 * ================================================================================
 * Open Source License
 * ================================================================================
 * This version of Sencha GXT is licensed under the terms of the Open Source GPL v3
 * license. You may use this license only if you are prepared to distribute and
 * share the source code of your application under the GPL v3 license:
 * http://www.gnu.org/licenses/gpl.html
 *
 * If you are NOT prepared to distribute and share the source code of your
 * application under the GPL v3 license, other commercial and oem licenses
 * are available for an alternate download of Sencha GXT.
 *
 * Please see the Sencha GXT Licensing page at:
 * http://www.sencha.com/products/gxt/license/
 *
 * For clarification or additional options, please contact:
 * licensing@sencha.com
 * ================================================================================
 *
 *
 * ================================================================================
 * Disclaimer
 * ================================================================================
 * THIS SOFTWARE IS DISTRIBUTED "AS-IS" WITHOUT ANY WARRANTIES, CONDITIONS AND
 * REPRESENTATIONS WHETHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE
 * IMPLIED WARRANTIES AND CONDITIONS OF MERCHANTABILITY, MERCHANTABLE QUALITY,
 * FITNESS FOR A PARTICULAR PURPOSE, DURABILITY, NON-INFRINGEMENT, PERFORMANCE AND
 * THOSE ARISING BY STATUTE OR FROM CUSTOM OR USAGE OF TRADE OR COURSE OF DEALING.
 * ================================================================================
 */
theme {
  name = "activitynfo"
  basePackage = "org.activityinfo.theme"

  headerText {
    color = "#ffffff"
    family = overpass
    size = "13px"
    weight = "bold"
  }

  insoBlue = "#4a6da1"
  overpass = "Overpass, sans-serif"
  overpassMono = "Overpass Mono, monospace"

  panelBackgroundColor = "#ffffff"

  borderColor = insoBlue

  details {
    borderColor = theme.borderColor
    backgroundColor = theme.panelBackgroundColor
    disabledOpacity = 0.5
    disabledTextColor = ""

    button {
      borderRadius = 4
      border = util.border('solid', '#126DAF', 1)
      overBorder = util.border('solid', '#126DAF', 1)
      pressedBorder = util.border('solid', '#126DAF', 1)
      // focusBoxShadow = "none"
      radiusMinusBorderWidth = util.radiusMinusBorderWidth(border, borderRadius)
      padding = util.padding(1);
      arrowColor = "#ffffff"

      backgroundColor = "#4B9CD7"
      overBackgroundColor = "#4792C8"
      pressedBackgroundColor = "#2A6D9E"
      gradient = '#4B9CD7 0%, #3892D3 50%, #358AC8 51%, #3892D3'
      overGradient = '#4792C8, #3386C2 50%, #307FB8 51%, #3386C2'
      pressedGradient = '#2A6D9E, #276796 50%, #2A6D9E 51%, #3F7BA7'

      font = util.fontStyle(overpass, '12px', '#FFFFFF', 'bold');
      smallFontSize = 12
      smallLineHeight = 18
      mediumFontSize = 14
      mediumLineHeight = 24
      largeFontSize = 16
      largeLineHeight = 32
    }

    buttonGroup {
      borderRadius = 3
      border = util.border('solid', '#dfeaf2', 3)
      headerGradient = util.solidGradientString('#dfeaf2')

      font = util.fontStyle(overpass, '13px', '#666666');
      headerPadding = util.padding(2);

      bodyPadding = util.padding(4);
      bodyBackgroundColor = '#FFFFFF'
    }

    field {
      backgroundColor = "#ffffff"
      borderColor = "#c5d1da"
      borderWidth = 1
      borderStyle = "solid"
      /* TODO: border-radius: 2px */
      emptyTextColor = "#808080"
      focusBorderColor = insoBlue
      height = 44
      invalidBackgroundColor = "#ffffff"
      invalidBorderColor = '#D94E37'
      invalidBorderWidth = 1
      lineHeight = "18px"
      padding {
        top = 12
        left = 8
        right = 8
        bottom = 12
      }
      text = util.fontStyle(overpassMono, '14px', '#4a4a4a', 400);
      checkBox {
         boxLabel = text;
         padding {
           bottom = 0
           left = 19
           right = 10
           top = 0
         }
      }
      radio {
        boxLabel = text
        padding {
          bottom = 0
          left = 19
          right = 10
          top = 0
        }
      }
      slider {
        trackHeight = 8
        trackBorder = util.border('solid', "#d4d4d4", 1)
        trackBackgroundColor = "#f5f5f5"
        trackRadius = 4

        thumbWidth = 15
        thumbHeight = thumbWidth
        thumbRadius = 8
        thumbBorder = util.border("solid", "#777777", 1)
        thumbBackgroundColor = "#f5f5f5"
      }

      sideLabel {
        text = field.text
        textAlign = 'left'
        padding = util.padding(0, 0, 3)
        labelPadding = util.padding(5, 5, 0, 0)
        fieldPadding = util.padding(0)
      }
      topLabel {
        text = field.text
        textAlign = 'left'
        padding = util.padding(0, 0, 5)
        labelPadding = util.padding(6, 0, 0)
        fieldPadding = util.padding(0)
      }
    }

    datePicker {
      border = util.border("solid", "#e1e1e1", 1)
      backgroundColor = '#ffffff'

      headerPadding = util.padding(8, 6)
      headerBackgroundColor = "#f5f5f5"
      headerText = util.fontStyle(overpass, '12px', '#3892d3', 'bold')
      headerTextPadding = util.padding(0, 5)

      dayOfWeekBackgroundColor = '#ffffff'
      dayOfWeekText = util.fontStyle(overpass, '13px', '#000000', 'bold')
      dayOfWeekLineHeight = '24px'
      dayOfWeekPadding = util.padding(0, 9, 0, 0)

      dayBorder = util.border('solid', '#ffffff', 1)
      dayText = util.fontStyle(overpass, '13px', '#000000')
      dayLineHeight = '23px'
      dayPadding = util.padding(0, 3, 0, 0)

      dayDisabledBackgroundColor = '#eeeeee'
      dayDisabledText = util.extend(dayText, {
        color = '#808080'
      })

      dayNextBackgroundColor = '#ffffff'
      dayNextText = util.extend(dayText, {
        color = '#bfbfbf'
      })

      dayPreviousBackgroundColor = '#ffffff'
      dayPreviousText = dayNextText

      itemOverBorder = util.border('solid', '#ffffff', 1)
      itemOverColor = "#000000"
      itemOverBackgroundColor = "#eaf3fa"

      itemSelectedBorder = util.border('solid', '#3892d3', 1)
      itemSelectedBackgroundColor = '#d6e8f6'
      itemSelectedText = util.extend(dayText, {
        weight = 'bold'
      })

      monthLeftButtonColor = "#ffffff"
      monthLeftButtonOpacity = 1
      monthLeftButtonMargin = util.margin(0, 0)
      monthRightButtonColor = "#ffffff"
      monthRightButtonMargin = util.margin(0, 0)
      monthRightButtonOpacity = 1

      todayBorder = util.border('solid', '#8b0000', 1)

      footerPadding = util.padding(3, 0)
      footerBackgroundColor = '#f5f5f5'

      buttonMargin = util.margin(0, 3, 0, 2)

      width = "212px"
    }

    panel {

      font = util.extend(headerText, {
        family = overpass
      })

      border = util.border('none')

      frameWidth = 0
      frameHeight = 0


      headerLineHeight = "15px"
      headerPadding = util.padding(10);
      padding = util.padding(0)

      backgroundColor = panelBackgroundColor
      headerBackgroundColor = "#157FCC"

      headerGradient = util.solidGradientString('#157FCC')
    }

    framedPanel = util.extend(panel, {
      borderRadius = 4
      border = util.border('solid', theme.borderColor, 5)

      headerLineHeight = "15px"
      headerPadding = util.padding(util.abs(panel.headerPadding.top - borderRadius), panel.headerPadding.right, panel.headerPadding.bottom)
      radiusMinusBorderWidth = util.radiusMinusBorderWidth(border, borderRadius)
    })

    window = util.extend(framedPanel, {
      border = util.border('solid', '#3291d6', 5)
      backgroundColor = "#ffffff"
      headerGradient = util.solidGradientString("#3291d6")
      headerLineHeight = "15px"
    })

    messagebox {
      text = util.fontStyle(overpass, '13px')
      messagePadding = util.padding(10, 10, 5)
      bodyPadding = util.padding(5, 10, 10)
      iconPadding = util.padding(10)
    }

    borderLayout {
      panelBackgroundColor = "#EBF1EF"
      collapsePanelBackgroundColor = "#157FCC"
      collapsePanelBorder = util.border('solid', theme.borderColor, 1)
    }
    splitbar {
      dragColor = "#B4B4B4"
      handleOpacity = 0.5
      handleWidth = 8
      handleHeight = 48
    }
    
    accordionLayout = util.extend(panel, {
      headerGradient = util.solidGradientString("#DFEAF2")
      headerBackgroundColor = "#DFEAF2"
      headerBarMargin = util.margin(0)
      headerPadding = util.padding(8, 10)
      font = util.extend(headerText, {
        color = '#666666'
      })
      panelPadding {
        bottom = 1
        left = 0
        right = 0
        top = 0
      }
      firstPanelPadding = panelPadding
      border = util.border('solid', '#ffffff', 0, 0, 1)
    })

    fieldset {
      backgroundColor = ""
      text = {
        family = overpassMono
        size = '13px'
        color = '#5d6f62'
        weight = 700
      }
      border = {
        style = 'solid'
        color = '#c5d1da'
        left = 0
        right = 0
        top = 0
        bottom = 0
      }

      legendPadding {
        top = 0
        bottom = 4
        left = 0
        right = 0
      }
      padding = util.padding(5)
      collapseIconColor = tools.primaryColor
      collapseOverIconColor = tools.primaryOverColor
      expandIconColor = collapseIconColor
      expandOverIconColor = collapseOverIconColor
    }

    toolbar {
      backgroundColor = "#ffffff"
      gradient = util.solidGradientString("#ffffff")
      border = util.border('none')
      padding = util.padding(3)

      buttonOverride = util.extend(theme.details.button, {
        border = util.border('solid', '#cecece', 1)
        overBorder = util.border('solid', '#cecece', 1)
        pressedBorder = util.border('solid', '#cecece', 1)

        arrowColor = "#666666"
        // focusBoxShadow = "none"

        backgroundColor = "#F6F6F6"
        overBackgroundColor = "#EDEDED"
        pressedBackgroundColor = "#E1E1E1"
        gradient = '#F6F6F6 0%, #F5F5F5 50%, #E8E8E8 51%, #F5F5F5 100%'
        overGradient = '#EDEDED 0%, #EBEBEB 50%, #DFDFDF 51%, #EBEBEB 100%'
        pressedGradient = '#E1E1E1 0%, #D5D5D5 50%, #E1E1E1 51%, #E4E4E4 100%'

        font = util.fontStyle(overpass, '12px', '#666666', 'bold');
      })
      htmlEditorIconColor = "#666666"

      labelItem {
        text = field.text
        lineHeight = '17px'
        padding = util.padding(2, 2, 0)
      }
      separatorBorder {
        bottom = 0
        color = "#d0d0d0"
        left = 0
        right = 0
        style = "solid"
        top = 0
      }
      separatorHeight = 16
    }
    status {
      text = util.fontStyle(overpass, '12px', '#000000')
      lineHeight = '16px'
      padding = util.padding(0, 5)
      border = util.border('solid', '#cccccc #d9d9d9 #d9d9d9', 1)
    }

    tools {
      primaryColor = util.mixColors("#ffffff", "#157FCC", 0.5)
      primaryOpacity = 1
      primaryOverColor = util.mixColors("#ffffff", "#157FCC", 0.3)
      primaryOverOpacity = 1
      primaryClickColor = util.mixColors("#ffffff", "#157FCC", 0.25)
      primaryClickOpacity = 1
      warningColor = "#D94E37"
      allowColor = "#C6E38A"

      tabs {
        tabCloseOver = "#ffffff"
      }
    }

    tabs {
      activeHeadingText = util.extend(headingText, {
        color = "#027dce"
      })
      activeTabItemBackgroundColor = "#add2ed"
      activeTabItemBorderBottom = "1px"
      activeTabItemBorderTop = "1px"
      activeTabItemMarginBottom = "-1px"

      activeTabCloseIconOpacity = 0.8
      activeTabCloseOverIconOpacity = 1
      tabCloseIconOpacity = 0.6
      tabCloseOverIconOpacity = 1

      borderRadius = 3
      borderColor = "#037ecf"
      bodyBackgroundColor = "#ffffff"
      closeIconWidth = 11
      gradient = util.solidGradientString("#add2ed")
      headingColor = "#1C94C4"
      headingText = util.extend(theme.headerText, {
        color = "#ffffff"
      })
      hoverGradient = util.solidGradientString("#5fa7db")
      hoverHeadingText = headingText
      hoverTabItemBackgroundColor = "#5fa7db"
      iconLeftOffset = 6
      iconTopOffset = 5
      iconWidth = 16
      inactiveGradient = util.solidGradientString("#4b9cd7")
      inactiveLastStopColor = "#4b9cd7"
      inactiveTabItemBackgroundColor = "#4b9cd7"
      lastStopColor = "#add2ed"
      overTabItemBorderBottom = "1px"
      overTabItemBorderTop = "1px"
      overTabItemOpacity = 1
      padding = util.padding(10)
      paddingWithClosable = util.padding(padding.right + closeIconWidth - 2)
      paddingWithIcon = util.padding(padding.left + iconWidth/2)
      plainActiveHeadingText = activeHeadingText
      plainHeadingText = headingText
      plainHoverHeadingText = hoverHeadingText
      plainTabBarBorderBottom = 1
      plainTabBarBorderTop = 1
      plainTabStripSpacerBorder = 1
      plainTabStripSpacerHeight = 2
      scrollerBackgroundColor = "#007cd1"
      scrollerLeftDisabledOpacity = 0.7
      scrollerLeftOverOpacity = 0.7
      scrollerRightDisabledOpacity = 0.7
      scrollerRightOverOpacity = 0.7
      scrollerWidth = 18
      tabHeight = 31
      tabBarBorder = "none"
      tabBarBottomHeight = 4
      tabBodyBorder = "none"
      tabItemBorderBottom = "none"
      tabItemBorderLeft = "none"
      tabItemBorderTop = "none"
      tabItemBorderRight = "none"
      tabSpacing = 1
      tabStripBottomBorder = "none"
      tabStripBackgroundColor = "#037ecf"
      tabStripGradient = util.solidGradientString('#037ecf')
      tabStripPadding = util.padding(0)
      tabStripTopBorder = "none"
      tabTextPadding = util.padding(10, 0)
    }

    tree {
      checkboxMargin = util.margin(4, 3, 0, 2)

      dragOverBackgroundColor = '#e2eff8'

      dropBackgroundColor = '#e2eff8'

      iconMargin = util.margin(0, 3, 0, 2)

      itemHeight = '25px'

      nodePadding = util.padding(0, 6, 0, 3)

      overBackgroundColor = '#e2eff8'

      selectedBackgroundColor = '#c1ddf1'

      text = util.fontStyle(overpass, '13px', '#000000')
    }

    info {
      backgroundColor = "#ffffff"
      borderRadius = 8
      opacity = 1.0
      border = util.border('solid', '#cccccc', 2)
      radiusMinusBorderWidth = util.radiusMinusBorderWidth(border, borderRadius)
      headerPadding = util.padding(0,0,8,0)
      headerText = util.fontStyle(overpass, '15px', '#555555', 'bold');
      messagePadding = util.padding(0)
      messageText = util.fontStyle(overpass, '14px', '#555555');
      margin = util.margin(2,0,0,0)
      padding = util.padding(2,7)
    }
    tip {
      backgroundColor = "#eaf3fa"
      borderRadius = 3
      opacity = 1
      border = util.border('solid', '#e1e1e1', 1)
      radiusMinusBorderWidth = util.radiusMinusBorderWidth(border, borderRadius)
      headerPadding = util.padding(0)
      headerText = util.fontStyle(overpass, '12px', '#000000', 'bold');
      messagePadding = util.padding(0,0,0)
      messageText = util.fontStyle(overpass, '12px');
      margin = util.margin(0)
      padding = util.padding(3)
    }
    errortip = tip

    grid {
      bodyBorder {
        bottom = 0
        color = "#d0d0d0"
        left = 0
        right = 0
        style = "solid"
        top = 0
      }
      columnHeader {
      /*  backgroundColor = '#f5f5f5' */
        borderWidth = 0
        borderColor = '#c0c0c0'
        borderStyle = 'solid'
        gradient = ''
        overBackgroundColor = '#eef6fb'
        overGradient = ''

        text = util.fontStyle(overpassMono, '11px', '#4a6da1', '600')
        padding = util.padding(7, 10)
        lineHeight = "15px"

        menuButtonWidth = 18

        menuBackgroundColor = '#f5f5f5'
        menuGradient = gradient
        menuBorder = util.border('solid', '#c0c0c0', 0,0,0,1)
        menuHoverBackgroundColor = '#eef6fb'
        menuHoverGradient = overGradient
        menuHoverBorder = menuBorder
        menuActiveBackgroundColor = '#DFEAF2'
        menuActiveGradient = "#DFEAF2 0%, #DFEAF2 100%"
        menuActiveBorder = menuBorder
      }

      cellPadding = {
        left = 8
        right = 16
        top = 12
        bottom = 12
      }
      cellText = util.fontStyle(overpass, '14px', 'rgb(74, 74, 74)', 400)
      cellLineHeight = "18.2px"
      cellVBorderColor = 'rgba(101, 116, 127, 0.3)'
      cellHBorderColor = 'rgba(101, 116, 127, 0.3)'
      cellBorderWidth = 1
      cellBackgroundColor = '#F0F0F0'
      cellAltBackgroundColor = '#FFFFFF'

      cellOverVBorderColor = '#ededed'
      cellOverVBorderStyle = 'solid'
      cellOverHBorderColor = '#e2eff8'
      cellOverHBorderStyle = 'solid'

      cellOverBackgroundColor = '#e2eff8'

      cellSelectedVBorderColor = '#ededed'
      cellSelectedVBorderStyle = 'solid'
      cellSelectedHBorderColor = '#e2eff8'
      cellSelectedHBorderStyle = 'solid'

      cellSelectedBackgroundColor = '#c1ddf1'

      specialColumnGradient = ""
      specialColumnGradientSelected = ""

      group {
        backgroundColor = '#f5f5f5'
        border = util.border('solid', '#c0c0c0', 0, 0, 1)
        text = columnHeader.text
        padding = util.padding(8, 4)
        iconSpacing = 11 + 6 // icon is 11px wide, plus 6px padding
        summary {
          text = util.extend(columnHeader.text, {
            color = '#000000'
          })
          backgroundColor = '#ffffff'
        }
      }

      rowNumberer {
        text = util.fontStyle(overpass, '13px', '#000000')
        padding = util.padding(5,3,4)
      }

      rowEditor {
        backgroundColor = '#DFEAF2'
        border = util.border('solid', '#e1e1e1', 1, 0);
      }

      footer {
        text = util.fontStyle(overpass, '13px', '#000000', 'bold')
        backgroundColor = '#ffffff'
        cellBorder = util.border('solid', '#ededed', 1, 0, 0)
      }
    }
    listview {
      text = util.fontStyle(overpass,'13px');
      lineHeight = '22px'
      backgroundColor = '#ffffff'
      border = util.border('solid', '#e1e1e1', 1)

      item {
        backgroundColor = "#ffffff"
        padding = util.padding(0,6)
        border = util.border('none')
        gradient = util.solidGradientString('#ffffff')
      }

      overItem {
        backgroundColor = "#d6e8f6"
        padding = util.padding(0,6)
        border = util.border('none')
        gradient = util.solidGradientString('#d6e8f6')
      }

      selectedItem {
        backgroundColor = "#c1ddf1"
        padding = util.padding(0,6)
        border = util.border('none')
        gradient = util.solidGradientString('#c1ddf1')
      }
    }
    menu {
      backgroundColor = "#ffffff"
      border = util.border('solid', '#e1e1e1', 1)
      padding = util.padding(0)
      gradient = util.solidGradientString('#ffffff')
      lastGradientColor = '#ffffff'

      itemText = util.fontStyle(overpass, '13px')
      itemLineHeight = '24px'
      itemPadding = util.padding(0)

      activeItemText = itemText
      activeItemBackgroundColor = '#d6e8f6'
      activeItemGradient = util.solidGradientString('#d6e8f6')
      activeItemBorder = util.border('none')

      bar {
        backgroundColor = menu.backgroundColor
        border = util.border('none')
        padding = util.padding(0)
        gradient = menu.gradient


        itemText = menu.itemText
        itemLineHeight = menu.itemLineHeight
        itemPadding = util.padding(1, 8)

        hoverItemText = itemText
        hoverItemBackgroundColor = menu.activeItemBackgroundColor
        hoverItemGradient = menu.activeItemGradient
        hoverItemBorder = util.border('none')

        activeItemText = itemText
        activeItemBackgroundColor = menu.activeItemBackgroundColor
        activeItemGradient = menu.activeItemGradient
        activeItemBorder = util.border('none')
      }

      scrollerHeight = 8
      separator {
        height = 1
        margin = util.margin(2, 3)
        color = '#e1e1e1'
      }

      header {
        border = util.border('solid', '#99bbe8', 0, 0, 1)
        backgroundColor = '#D6E3F2'

        itemText = util.fontStyle(overpass, '10px', '#15428b', 'bold')
        itemLineHeight = '13px'

        itemPadding = util.padding(3)
      }
    }
    mask {
      opacity = 0.7
      backgroundColor = '#ffffff'
      box {
        padding = util.padding(5)
        borderColor = ''
        borderStyle = 'none'
        borderWidth = 0
        borderRadius = 3
        radiusMinusBorderWidth = util.max(0, borderRadius - borderWidth)
        backgroundColor = '#e5e5e5'
        text = util.fontStyle(overpass, '13px', '#666666')
        textPadding = util.padding(21, 0, 0)
        loadingImagePosition = 'center 0'
      }
    }
    progressbar {
      border = util.border('none')
      text = util.fontStyle(overpass, '13px', '#666666', 'bold')
      backgroundGradient = ''//transparent
      textPadding = util.padding(3, 0)
      textAlign = 'center'
      barTextColor = text.color
      barGradient = util.solidGradientString('#c1ddf1')
      barBorder = util.border('none')
    }

    statusproxy {
      text = util.fontStyle(overpass, '13px')
      border = util.border('solid', '#dddddd #bbbbbb #bbbbbb #dddddd', 1)
      backgroundColor = '#ffffff'
      opacity = 0.85
    }
    colorpalette {
      itemSize = 16
      itemPadding = util.padding(2)
      backgroundColor = '#ffffff'
      itemBorder = util.border('solid', '#e1e1e1', 1)
      selectedBackgroundColor = '#e6e6e6'
      selectedBorder = util.border('solid', '#8bb8f3', 1)
    }
  }
}