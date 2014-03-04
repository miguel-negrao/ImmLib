/*
    ImmLib
    Copyright 2013 Miguel Negrao.

    ImmLib: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/


MUEvNetTModDef : UEvNetTModDef {

    /*asUModFor { |unit|
        ^UEvNetTMod(this).asUModFor(unit)
    }*/

	 /*
    because this is an animation we use signals which are on sampled once per update using the <@ operator.
    */
	addReactimatesFunc { |unit, tEventSource|
        ^{ |dict|
             if( dict.isEmpty ) {
                EventNetwork.returnUnit
            } {
                dict.collect{ |uarg, key|
                    uarg.match({ |sig|
                        (sig <@ tEventSource).collect{ |v| IO{
                            unit.items.do{ |u,i|
                                u.mapSet(key, v[i])
                            }
                        } }.reactimate
                        },{ |sig|
                            (sig <@ tEventSource).collect{ |v| IO{
                                unit.items.do{ |u,i|
                                    u.set(key, v[i] )
                                }
                            } }.reactimate
                    })
            }.as(Array).reduce('>>=|') }
        }

    }
}

MUENTModDef : UENTModDef {

    /*asUModFor { |unit|
        ^UENTMod(this).asUModFor(unit)
    }*/

	 /*
    because this is an animation we use signals which are on sampled once per update using the <@ operator.
    */
	addReactimatesFunc { |unit, tEventSource|
        ^{ |dict|
             if( dict.isEmpty ) {
                EventNetwork.returnUnit
            } {
                dict.collect{ |uarg, key|
					Object.checkArgs(MUENTModDef, \addReactimatesFunc, [uarg,key], [UModArg, Symbol]);
                    uarg.match({ |sig|
                        (sig <@ tEventSource).collect{ |v| IO{
                            unit.items.do{ |u,i|
                                u.mapSet(key, v[i])
                            }
                        } }.reactimate
                        },{ |sig|
                            (sig <@ tEventSource).collect{ |v| IO{
                                unit.items.do{ |u,i|
                                    u.set(key, v[i] )
                                }
                            } }.reactimate
                    })
            }.as(Array).reduce('>>=|') }
        }

    }

}

ImmMod : UEvNetTMod {
	var sliderValues;
	var <sliderProxys;

	*new { |def, sliderValues|
		^super.new(def).initImmMod(def, sliderValues)
    }

	initImmMod { |def, sliderValuesArgs|
		var temp = sliderValuesArgs.asArray.clump(2).collect{ |xs| xs[0].asSymbol -> xs[1] }.asIdentDictFromAssocs;
		sliderValues = def.sliderSpecs.clump(2).collect{ |xs|
			var key = xs[0];
			var x = temp.at(key);
			if( x.isNil) { xs[1].default}{ x }
		};
	}

	asUModFor { |unit|
        var tESM;
        timer = ENTimer(desc.delta);
		sliderProxys = sliderValues.collect{ |v|  FRPGUIProxy(nil, v) };
        tESM = timer.asENInput;
        tES = tESM.a;
		eventNetwork = EventNetwork( desc.createDesc(unit, tESM, sliderProxys.collect(_.asENInput) ) );
        eventNetwork.actuateNow;
    }

	sliderWindow {
		var specspairs = desc.sliderSpecs.clump(2).flop;
		var labels = specspairs[0];
		var specs = specspairs[1];
		var sliders = [this.sliderValues, specs].flopWith{ |v, spec| Slider().value_(spec.unmap(v)) };
		var numboxes = [sliders, specs].flopWith{ |sl, spec|
			var x = NumberBox();
			sl.addAction{ |v| x.value_(spec.map(v.value)) };
			x
		};
		var ezsliders = [sliders, labels, numboxes ].flopWith{ |sl, l, numbox|
			VLayout( StaticText().string_(l), sl, numbox )
		};
		[sliders, sliderProxys].flopWith{ |sl, proxy| proxy.view_(sl) };
		^Window().layout_(HLayout(*ezsliders)).front
	}

	sliderValues {
		^if(sliderProxys.isNil){
			sliderValues
		} {
			sliderProxys.collect( _.value )
		}
	}

	gui { |parent, bounds|
		if( desc.sliderSpecs.size > 0) {
			var specspairs = desc.sliderSpecs.clump(2).flop;
			var labels = specspairs[0];
			var specs = specspairs[1];
			var viewHeight = 14;
			[this.sliderValues, specs, labels, sliderProxys].flopWith{ |v, spec, label, proxy|
				var bounds2 = (bounds.width @ ((spec.viewNumLines * viewHeight) + ((spec.viewNumLines-1) * 4)));
				var composite = CompositeView( parent, bounds2 ).resize_(2);
				var viewDict = spec.makeView( composite, bounds2, "*"++label,
					{ |vw, value| }, 5
				);
				viewDict[ \valueView ].value = v;
				proxy.view_(viewDict[ \valueView ])
			}
		}
	}

	viewNumLines{
		^sliderValues.size;
	}

	storeArgs {
		^[desc, [this.desc.sliderSpecs.clump(2).flop[0], this.sliderValues].flop.flatten ]
    }
}

ImmDef : MUENTModDef {
	classvar <currentSurface;
	classvar <currentTimeES;
	var <surface;
	var <sliderSpecs;
	/*
	sliderSpecs:
	[ "slider1", \freq, "slider"2, [1,5] , "slider3", nil ]
	*/

	*new { |descFunc, surface = (PSurface.geodesicSphere), delta = 0.1,
		sliderSpecs=#[] |
		var check1 = if(sliderSpecs.size.odd){ Error("ImmDef - sliderSpecs: array size must be even").throw };
		var sliderSpecs2 = sliderSpecs.clump(2).collect{ |xs| [xs[0].asSymbol, xs[1].asControlSpec] }.flatten;

		this.checkArgs(\ImmDef, \new, [descFunc, surface, delta, sliderSpecs2],
			[Function, PSurface, SimpleNumber, Array] );

        ^super.newCopyArgs(descFunc, delta, surface, sliderSpecs2)
    }

    createDesc { |unit, tESM, slidersM|
		this.checkArgs(\ImmDef, \createDesc, [unit, tESM], [MU, Writer] );
        ^tESM >>= { |tEventSource|
            var tSignal = tEventSource.hold(0.0);
			currentSurface = surface;
			currentTimeES = tEventSource;
			slidersM.sequence(EventNetwork) >>= { |sliderSigs|
				ENDef.evaluate( descFunc, [tSignal]++sliderSigs )
				>>= this.addReactimatesFunc(unit, tEventSource)
			}
        }
    }

	test{ |startTime = 0|
		var tESM, eventNetwork, timer;
		this.checkArgs(\ImmDef, \test, [startTime], [SimpleNumber] );
        timer = ENTimer(delta);
        tESM = timer.asENInput;
        eventNetwork = EventNetwork(
			tESM >>= { |tEventSource|
            var tSignal = tEventSource.hold(0.0);
			currentSurface = surface;
			ENDef.evaluate( descFunc, [tSignal] )
        } );
        eventNetwork.actuateNow;
		timer.start(startTime).unsafePerformIO;
		^eventNetwork
	}


	asUModFor { |unit|

        ^ImmMod(this).asUModFor(unit, delta)

    }

	storeArgs {
		^[descFunc, surface, delta,  sliderSpecs ]
    }

}




















