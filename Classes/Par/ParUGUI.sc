ParUGUI : UGUI {

	*viewNumLines { |unit|
		^(unit.argSpecsForDisplay ? [])
			.collect({|x|
				if( unit[ x.name ].isKindOf( ParUMap ) ) {
					ParUMapGUI.viewNumLines( unit[ x.name ] );
				} {
					x.spec.viewNumLines
				};
			}).sum
		+ 1 + if( unit.mod.notNil and: { unit.mod.isKindOf( UEvNetMod ) } ) { unit.mod.viewNumLines } {0};
	}

	makeSubViews { |bounds|
		var mod = unit.mod;
		var addBefore;
		views = ();

		this.makeHeader(bounds);

		if( GUI.id == \cocoa ) { View.currentDrag = nil; };

		addBefore = UserView( composite, bounds.width@6 )
		.resize_(2);

		addBefore.background_( Color.white.alpha_(0.25) );
		addBefore.canReceiveDragHandler_({ |sink|
			View.currentDrag.isKindOf(UEvNetModDef)
		})
		.receiveDragHandler_({ |sink, x, y|
			var mod = unit.mod;
			var ii;
			var drag = View.currentDrag;
			if ( drag.isKindOf( UEvNetModDef ) ) {
				if( mod.notNil and: { mod.isKindOf(UEvNetTMod) } ) {
					mod.def_( drag.name );
					unit.changed( \mod )
				} {
					unit.mod_( drag.asMod )
				};
				mapSetAction.value( this );
			}
		});

		if( mod.notNil and: { mod.isKindOf( UEvNetMod ) } ) {
			mod.makeView( composite, bounds, unit, { mapSetAction.value( this ) } )
		};

		unit.argSpecsForDisplay.do({ |argSpec, i|
			var vw, key, value;
			var decLastPos;
			var umapdragbin;
			var umapdragbinTask;
			var viewNumLines;

			key = argSpec.name;
			value = unit.at( key );

			if( argSpec.notNil ) {
				if( value.isUMap ) {
					vw = ParUMapGUI( composite, composite.bounds.insetBy(0,-24), value );
					vw.parentUnit = unit;
					vw.mapSetAction = { mapSetAction.value( this ) };
					vw.removeAction = { |umap|
						if( unit.isKindOf( MassEditU ) ) {
							umap.units.do({ |item|
								if( item.isUMap ) { item.stop };
							});
							unit.units.do({ |item|
								if( item.get( key ).isUMap ) {
									item.removeUMap( key );
								};
							});
						} {
							umap.stop;
							unit.removeUMap( key );
						};
					};
				} {
					//"value is : %".format(value).postln;
					if( value.isKindOf(ParArg) ){
						var spec = argSpec.spec.parEditSpec(unit.get(key).array, unit.n);
						//"spec is %".format(spec).postln;
						if(spec.isNil){
							//can't display for this spec just static text
							/*var labelWidth = 80; //(RoundView.skin ? ()).labelWidth ? 80;
							StaticText( composite, Rect(0,0, labelWidth, 14 ) )
							.string_( key.asString ++ " ")
							.align_( \right )
							.resize_( 4 )
							.applySkin( RoundView.skin );*/
							/*StaticText( composite, Rect(labelWidth + 4,0, bounds.width - labelWidth - 4 - 4 - 14 ) )
							.string_( value.cs)
							.resize_( 4 )
							.applySkin( RoundView.skin );*/
							vw = ParStaticObjectView( composite, bounds.width, unit, key,
								spec, controller,
								switch( argSpec.mode,
									\nonsynth, { key ++ " (l)" },
									\init, { key ++ " (i)" }
								)
							);
							vw.redrawUChainGUIAction = { mapSetAction.value( this ) };
						}{
							vw = ParObjectView( composite, bounds.width, unit, key,
								spec, controller,
								switch( argSpec.mode,
									\nonsynth, { key ++ " (l)" },
									\init, { key ++ " (i)" }
								)
							);
							vw.pausable_( unit.mod !? { |mod| mod.keySignalDict } !? { |dict| dict.keys.includes(key) } );
							vw.testValue = { |value| value.isKindOf( UMap ).not };
							vw.action = { action.value( this, key, value ); };
							vw.redrawUChainGUIAction = { mapSetAction.value( this ) };
						}
					} {
						vw = SingleObjectView( composite, bounds.width, unit, key,
							argSpec.spec, controller,
							switch( argSpec.mode,
								\nonsynth, { key ++ " (l)" },
								\init, { key ++ " (i)" }
							)
						);
						vw.testValue = { |value| value.isKindOf( UMap ).not };
						vw.action = { action.value( this, key, value ); };
						vw.redrawUChainGUIAction = { mapSetAction.value( this ) };
					};


					if( [ \nonsynth ].includes(argSpec.mode).not ) {
						viewNumLines = argSpec.spec.viewNumLines;
						composite.decorator.nextLine;
						composite.decorator.shift( 0,
							((viewHeight + composite.decorator.gap.y) * viewNumLines).neg
						);

						umapdragbin = UserView( composite, labelWidth @ viewHeight )
						.canFocus_( false )
						.canReceiveDragHandler_({ |vw, x,y|
							var last;
							if( x.notNil ) {
								last = currentUMapSink;
								currentUMapSink = vw;
								last !? _.refresh;
								vw.refresh;
							};
							View.currentDrag.isKindOf( UMapDef ) && {
								unit.canUseUMap( key, View.currentDrag );
							};
						});
						umapdragbin.drawFunc = { |vw|
							if( View.currentDrag.notNil && {
								vw.canReceiveDragHandler.value == true;
							}) {
								Pen.width = 2;
								if( currentUMapSink === vw ) {
									Pen.color = Color.blue.alpha_(1);
								} {
									Pen.color = Color.blue.alpha_(0.25);
								};
								Pen.addRect( vw.bounds.moveTo(0,0).insetBy(1,1) );
								Pen.stroke;
								if( umapdragbinTask.isPlaying.not ) {
									umapdragbinTask = Task({
										while { vw.isClosed.not && {												vw.canReceiveDragHandler.value == true
										}
										} {
											0.25.wait;
										};
										if( vw.isClosed.not ) {
											vw.refresh;
										};
									}, AppClock).start;
								};
							};
						};

						if( unit.isKindOf( MassEditU ) ) {
							umapdragbin.receiveDragHandler_({
								unit.units.do({ |unit|
									unit.insertUMap( key, View.currentDrag );
								});
							});
						} {
							umapdragbin.receiveDragHandler_({
								unit.insertUMap( key, View.currentDrag );
							});
						};
						composite.decorator.nextLine;
						composite.decorator.shift( 0,
							((viewHeight + composite.decorator.gap.y) * (viewNumLines - 1))
						);
					};
					//};
				};
				views[ key ] = vw;
			}

		});

		if( views.size == 0 ) {
			controller.remove;
			mapCheckers.do(_.remove);
		};
	}

}

