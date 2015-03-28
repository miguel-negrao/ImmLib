/*
		(C)opyright 2013-2015 by Miguel Negrão

    This file is part of ImmLib.

		ImmLib is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

		ImmLib is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImmLib.  If not, see <http://www.gnu.org/licenses/>.
*/

ParUChainGUI : UChainGUI {

	makeUnitSubViews { |scrollView, units, margin, gap|
		var unitInitFunc;
		var comp, uview;
		var addLast, ug, header;
		var width;
		var notMassEdit;
		var scrollerMargin = 16;
		var realIndex = 0;
		var massEditWindow;

		if( GUI.id == \qt ) { scrollerMargin = 20 };

		notMassEdit = chain.class != MassEditUChain;


		width = scrollView.bounds.width - scrollerMargin - (margin.x * 2);

		unitInitFunc = { |unit, what ...args|
			if( what === \init ) { // close all views and create new
				chain.changed( \units );
			};
		};

		if( units.size == 0 ) {
			comp = CompositeView( scrollView, width@100 )
			.resize_(2);

			header = StaticText( comp, comp.bounds.width @ 14 )
			.applySkin( RoundView.skin )
			.string_( " empty: drag unit or Udef here" )
			.background_( Color.yellow.alpha_(0.25) )
			.resize_(2)
			.font_(
				(RoundView.skin.tryPerform( \at, \font ) ??
					{ Font( Font.defaultSansFace, 12) }).boldVariant
			);

			uview = UserView( comp, comp.bounds.width @ 100 );
			uview.background_( Color.white.alpha_(0.25) );

			uview.canReceiveDragHandler_({ |sink|
				var drg;
				drg = View.currentDrag;
				case { drg.isUdef }
				{ true }
				{ drg.isKindOf( UnitRack ) }
				{ true }
				{ [ Symbol, String ].includes( drg.class ) }
				{ Udef.all.keys.includes( drg.asSymbol ) }
				{ drg.isKindOf( ParU ) }
				{ true }
				{ false }
			})
			.receiveDragHandler_({ |sink, x, y|
				case { View.currentDrag.isKindOf( ParU ) } {
					chain.units = [ View.currentDrag.deepCopy ];
				}{ View.currentDrag.isUdef }{
					chain.units = [ /*ParU( chain.n, View.currentDrag )*/chain.createNewUnitFromSymbol( View.currentDrag.asSymbol ) ];
				}{ View.currentDrag.isKindOf( UnitRack ) } {
					chain.units = View.currentDrag.units;
				}{   [ Symbol, String ].includes( View.currentDrag.class )  } {
					chain.units = [ /*ParU( chain.n, View.currentDrag.asSymbol )*/chain.createNewUnitFromSymbol( View.currentDrag.asSymbol ) ];
				};
			})
		};

		ug = units.collect({ |unit, i|
			var header, comp, uview, plus, min, defs, io;
			var addBefore, indexLabel, ugui;
			var currentUMaps;
			var massEditWindowButton;

			indexLabel = realIndex.asString;

			if( notMassEdit && { unit.isKindOf( MassEditU ) } ) {
				realIndex = realIndex + unit.units.size;
				indexLabel = indexLabel ++ ".." ++ (realIndex -1);
			} {
				realIndex = realIndex + 1;
			};

			addBefore = UserView( scrollView, width@6 )
			.resize_(2);

			if( notMassEdit ) {
				addBefore.background_( Color.white.alpha_(0.25) );
				addBefore.canReceiveDragHandler_({ |sink|
					var drg;
					drg = View.currentDrag;
					case { drg.isUdef }
					{ true }
					{ drg.isKindOf( UnitRack ) }
					{ true }
					{ [ Symbol, String ].includes( drg.class ) }
					{ Udef.all.keys.includes( drg.asSymbol ) }
					{ drg.isKindOf( ParU ) }
					{ true }
					{ false }
				})
				.receiveDragHandler_({ |sink, x, y|
					var ii;
					case { View.currentDrag.isKindOf( ParU ) } {
						ii = units.indexOf( View.currentDrag );
						if( ii.notNil ) {
							units[ii] = nil;
							units.insert( i, View.currentDrag );
							this.setUnits( units.select(_.notNil) );
						} {
							this.setUnits(
								units.insert( i, View.currentDrag.deepCopy )
							);
						};
					} { View.currentDrag.isUdef } {
						this.setUnits( units.insert( i, /*ParU( chain.n, View.currentDrag )*/chain.createNewUnitFromSymbol( View.currentDrag.asSymbol ) ) );
					}{ View.currentDrag.isKindOf( UnitRack ) } {
						this.setUnits(
							units[..i-1] ++ View.currentDrag.units ++ units[i..]
						);
					}{   [ Symbol, String ].includes( View.currentDrag.class )  } {
						this.setUnits(
							units.insert( i, /*ParU( chain.n, View.currentDrag.asSymbol )*/chain.createNewUnitFromSymbol( View.currentDrag.asSymbol ) )
						);
					};
				});
			} {
				addBefore.canFocus = false;
			};

			comp = CompositeView( scrollView, width@14 )
			.resize_(2);

			header = StaticText( comp, comp.bounds.moveTo(0,0) )
			.applySkin( RoundView.skin )
			.string_( " " ++ indexLabel ++ ": " ++ if(unit.def.class == LocalUdef){"[Local] "}{""}++unit.defName )
			.background_( if( notMassEdit )
				{ Color.white.alpha_(0.5) }
				{ Color.white.blend( Color.yellow, 0.33 ).alpha_(0.5) }
			)
			.resize_(2)
			.font_(
				(RoundView.skin.tryPerform( \at, \font ) ??
					{ Font( Font.defaultSansFace, 12) }).boldVariant
			);


			//if( chain.class != MassEditUChain )

			uview = UserView( comp, comp.bounds.moveTo(0,0) );

			uview.canReceiveDragHandler_({ |sink|
				var drg;
				drg = View.currentDrag;
				case { drg.isUdef }
				{ true }
				{ drg.isKindOf( UnitRack ) }
				{ true }
				{ [ Symbol, String ].includes( drg.class ) }
				{ Udef.all.keys.includes( drg.asSymbol ) }
				{ drg.isKindOf( ParU ) }
				{ true }
				{ false }
			})
			.receiveDragHandler_({ |sink, x, y|
				var u, ii;
				case { View.currentDrag.isKindOf( ParU ) } {
					u = View.currentDrag;
					ii = units.indexOf( u );
					if( ii.notNil ) {
						units[ii] = unit;
						units[i] = u;
					} {
						units[ i ] = u.deepCopy;
					};
					this.setUnits( units );

				} { View.currentDrag.isKindOf( UnitRack ) } {
					this.setUnits( units[..i-1] ++ View.currentDrag.units ++ units[i+1..] );
				} { View.currentDrag.isUdef } {
					unit.def = View.currentDrag;
				} {   [ Symbol, String ].includes( View.currentDrag.class )  } {
					unit.def = View.currentDrag.asSymbol.asUdef;
				};
			})
			.beginDragAction_({
				unit;
			});

			if( unit.isKindOf( MassEditU ) ) {
				massEditWindowButton = SmoothButton( comp,
					Rect( comp.bounds.right -
						((18 + 2) + if( notMassEdit){12 + 4 + 12}{0}),
						1, 18, 12 )
				)
				.label_( 'up' )
				.border_( 1 )
				.radius_( 2 )
				.action_({
					var allUnits, userClosed = true;
					if( massEditWindow.notNil && { massEditWindow.isClosed.not }) {
						massEditWindow.close;
					};
					RoundView.pushSkin( skin );
					massEditWindow = Window( unit.defName,
						this.window.bounds.moveBy( this.window.bounds.width + 10, 0 ),
						scroll: true ).front;
					massEditWindow.addFlowLayout;
					comp.onClose_({
						if( massEditWindow.notNil && { massEditWindow.isClosed.not }) {
							userClosed = false;
							massEditWindow.close;
						};
					});
					allUnits = unit.units.collect({ |item, ii|
						var ugui;
						if( notMassEdit ) { ii = ii + (realIndex - unit.units.size) };
						StaticText( massEditWindow,
							(massEditWindow.bounds.width - 8 - scrollerMargin) @ 14 )
						.applySkin( RoundView.skin )
						.string_( " " ++ ii ++ ": " ++ item.defName )
						.background_( Color.white.alpha_(0.5) )
						.resize_(2)
						.font_(
							(RoundView.skin.tryPerform( \at, \font ) ??
								{ Font( Font.defaultSansFace, 12) }).boldVariant
						);
						massEditWindow.view.decorator.nextLine;
						ugui = item.gui( massEditWindow );
						ugui.mapSetAction = {
							chain.changed( \units );
						};
						[ item ] ++ item.getAllUMaps;
					}).flatten(1);
					allUnits.do({ |item|
						item.addDependant( unitInitFunc )
					});
					massEditWindowIndex = i;
					massEditWindow.onClose_({
						allUnits.do(_.removeDependant(unitInitFunc));
						if( userClosed ) {
							massEditWindowIndex = nil;
						};
					});
					RoundView.popSkin( skin );
				}).resize_(3);

				if( massEditWindowIndex == i ) {
					massEditWindowButton.doAction;
				};
			} {
				if( massEditWindowIndex == i ) {
					massEditWindowIndex = nil;
				};
			};

			if( notMassEdit ) {
				min = SmoothButton( comp,
					Rect( comp.bounds.right - (12 + 4 + 12), 1, 12, 12 ) )
				.label_( '-' )
				.border_( 1 )
				.action_({
					var u = unit;
					if( u.isKindOf( MassEditU ) ) {
						u = u.units.last;
					};
					chain.units = chain.units.select(_ != u);
				}).resize_(3);

				if( units.size == 1 ) {
					min.enabled = false;
				};

				plus = SmoothButton( comp,
					Rect( comp.bounds.right - (12 + 2), 1, 12, 12 ) )
				.label_( '+' )
				.border_( 1 )
				.action_({
					var copy;
					if( unit.isKindOf( MassEditU ) ) {
						unit.units = unit.units.add( unit.units.last.deepCopy.increaseIOs )
					} {
						units = units.insert( i+1, unit.deepCopy.increaseIOs );
					};
					this.setUnits( units );
				}).resize_(3);

				if(  unit.isKindOf( MassEditU ).not && { unit.audioOuts.size > 0 } ) {					SmoothButton( comp,
					Rect( comp.bounds.right - (45 + 2 + 12 + 4 + 12),
						1, 45, 12 )
				)
				.label_( "bounce" )
				.border_( 1 )
				.radius_( 2 )
				.action_({
					Dialog.savePanel( { |path|
						chain.bounce( chain.units.indexOf( unit ), path );
					});
				}).resize_(3);
				};
			};

			unit.addDependant( unitInitFunc );
			currentUMaps = unit.getAllUMaps;
			currentUMaps.do(_.addDependant( unitInitFunc ));
			header.onClose_({
				unit.removeDependant( unitInitFunc );
				currentUMaps.do(_.removeDependant( unitInitFunc ));
			});
			ugui = unit.gui( scrollView,
				scrollView.bounds.copy.width_(
					scrollView.bounds.width - scrollerMargin - (margin.x * 2)
				)
			);
			ugui.mapSetAction = { chain.changed( \units ) };
			ugui;
		});

		if( notMassEdit && { units.size > 0 } ) {
			addLast = UserView( scrollView, width@6 )
			.resize_(2)
			.background_( Color.white.alpha_(0.25) )
			.canFocus_(false);

			addLast.canReceiveDragHandler_({ |sink|
				var drg;
				drg = View.currentDrag;
				case { drg.isUdef }
				{ true }
				{ drg.isKindOf( UnitRack ) }
				{ true }
				{ [ Symbol, String ].includes( drg.class ) }
				{ Udef.all.keys.includes( drg.asSymbol ) }
				{ drg.isKindOf( ParU ) }
				{ true }
				{ false }
			})
			.receiveDragHandler_({ |sink, x, y|
				var ii;
				case { View.currentDrag.isKindOf( ParU ) } {
					ii = units.indexOf( View.currentDrag );
					if( ii.notNil ) {
						units[ii] = nil;
						this.setUnits( units.select(_.notNil) ++
							[ View.currentDrag ] );
					} {
						this.setUnits( units ++ [ View.currentDrag.deepCopy ] );
					};

				} { View.currentDrag.isUdef } {
					chain.units = chain.units ++ [ /*ParU( chain.n, View.currentDrag )*/chain.createNewUnitFromSymbol( View.currentDrag.asSymbol ) ];
				}{ View.currentDrag.isKindOf( UnitRack ) } {
					chain.units = chain.units ++ View.currentDrag.units;
				}{   [ Symbol, String ].includes( View.currentDrag.class )  } {
					chain.units = chain.units ++ [
						//ParU( chain.n, View.currentDrag.asSymbol )
						chain.createNewUnitFromSymbol( View.currentDrag.asSymbol );
					];
				};
			});
		};

		if( scrollViewOrigin.notNil ) {
			if( GUI.id == \qt ) {
				{
					scrollView.visibleOrigin = scrollViewOrigin; 					scrollViewOrigin = nil;
				}.defer(0.1);
			} {
				scrollView.visibleOrigin = scrollViewOrigin; 				scrollViewOrigin = nil;
			};
		};

		^ug;

	}

	makeUnitHeader { |units, margin, gap|
		var comp, header, min, io, defs, mapdefs, code;
		var notMassEdit, headerInset = 0;

		notMassEdit = chain.class != MassEditUChain;

		comp = CompositeView( composite, (composite.bounds.width - (margin.x * 2))@16 )
		.resize_(2);

		if( notMassEdit && { this.canPackUnits }) {
			RoundButton( comp, 13 @ 13 )
			.border_(0)
			.background_( nil )
			.label_([ 'down', 'play' ])
			.hiliteColor_(nil)
			.value_( packUnits.binaryValue )
			.action_({ |bt|
				chain.handlingUndo = true; // don't add a state
				this.packUnits = bt.value.booleanValue;
			});
			headerInset = 14;
		};

		header = StaticText( comp, comp.bounds.moveTo(0,0).insetAll( headerInset, 0,0,0 ) )
		.applySkin( RoundView.skin )
		.string_( if( notMassEdit ) { " units" } { " units (accross multiple events)" } )
		.align_( \left )
		.resize_(2);

		if( notMassEdit ) {
			io = SmoothButton( comp, Rect( comp.bounds.right - 60, 1, 60, 12 ) )
			.label_( "i/o" )
			.border_( 1 )
			.radius_( 2 )
			.action_({
				var parent;
				parent = composite.parent;
				{
					composite.remove;
					UChainIOGUI( parent, originalBounds, chain );
				}.defer(0.01);

			}).resize_(3);
			code = SmoothButton( comp,
				Rect( comp.bounds.right - (40 + 4 + 60), 1, 40, 12 ) )
			.label_( "code" )
			.border_( 1 )
			.radius_( 2 )
			.action_({
				var parent;
				parent = composite.parent;
				{
					composite.remove;
					UChainCodeGUI( parent, originalBounds, chain );
				}.defer(0.01);
			}).resize_(3);
		};

		defs = SmoothButton( comp,
			Rect( comp.bounds.right - (
				2 + 40 + (notMassEdit.binaryValue * (4 + 60 + 4 + 40))
			), 1, 42, 12
			)
		)
		.label_( "udefs" )
		.border_( 1 )
		.radius_( 2 )
		.action_({
			ImmUdefsGUI();
		}).resize_(3);

		CompositeView( comp, Rect( 0, 14, (composite.bounds.width - (margin.x * 2)), 2 ) )
		.background_( Color.black.alpha_(0.25) )
		.resize_(2);

	}


}
