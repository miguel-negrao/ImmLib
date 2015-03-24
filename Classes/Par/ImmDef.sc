ImmDef : UENTModDef {
	classvar <>currentSurface;
	classvar <>currentTimeES;
	var <surface;

	createDesc { |unit, tESM, slidersM|
		^tESM >>= { |tEventSource|
			var tSignal = tEventSource.hold(0.0);
			//"ImmDef unit is %".format(unit).postln;
			//"ImmDef surface is %".format(unit.surface).postln;
			//"ImmDef currentTimeES is %".format(tEventSource).postln;
			ImmDef.currentSurface = unit.surface;
			surface = unit.surface;
			ImmDef.currentTimeES = tEventSource;
			slidersM.sequence(EventNetwork) >>= { |sliderSigs|
				ENDef.evaluate( descFunc, [tSignal]++sliderSigs )
				.collect{ |dict|
					dict.collect{ |uarg|
						uarg.collect{ |signal|
							signal.collect( ParArg(_) )
						}
					}
				}
				>>= this.addReactimatesFunc(unit, tEventSource)
			}
		}
	}

	asMod {
		^ImmMod(this.name)
	}
}

ImmMod : UEvNetTMod {

	makeView { |parent, bounds, unit, redrawUChainGUIAction|
		var plotData; //:: Option[ (PGridPlot, Function) ]
		super.makeView(parent, bounds, unit, redrawUChainGUIAction);
		this.keySignalDict.select(_.isKindOf(USpecArg)).collect{ |uarg, key|
			var button;
			StaticText(parent, 50@14)
			.applySkin( RoundView.skin )
			.string_(key.asString);
			button = SmoothButton(parent, 14@14 )
			.border_( 1 )
			.radius_( 2 )
			.states_([["p"],["c"]])
			.action_{ |o|
				var sampledSig = (uarg.signal <@ tES);
				var closePlot = {
					plotData.do{ |t|
						var plot = t.at1;
						var f = t.at2;
						sampledSig.stopDoing(f);
						plot.quitRenderer;
					};
					plotData = None();
				};
				var reassign = {
					var newDict = this.keySignalDict;
					if(newDict.keys.includes(key)) {
						plotData.do{ |t|
							var action = t.at2;
							sampledSig.stopDoing(action);
							sampledSig = (newDict[key].signal <@ tES);
							sampledSig.do(action);
						} {
							closePlot.();
						}
					}
				};
				var modSC = SimpleController(this).put(\def,reassign);
				if( o.value == 1) {
					var plot = PGridPlot.basicNew(def.surface, "% | % | %".format(unit.defName, defName, key) );
					var f = { |p|
						plot.sendMsg(*(["/colors"]++p.array.collect{ |v|
							//[0.0,v.linlin(0.0,1.0,0.3,1.0),0.0]
							[0.0,v, 1-v]
						}.flat) )
					};
					var c = SimpleController(plot).put(\connected, {
						if(plot.connected.not){
							button.value_(0);
							c.remove;
							plotData.do{ |t|
								sampledSig.stopDoing(t.at2);
								plotData = None();
							};
						}
					} );
					plot.startRenderer;
					sampledSig.do(f);
					plotData = Some( T(plot, f) );
				} {
					closePlot.();
				}
			}
		}
	}

	/*
	store renderer for key, allow for only one open renderer. Reconnect to that renderer when def is changed.
	*/
	openRenderer { |key|

	}

}