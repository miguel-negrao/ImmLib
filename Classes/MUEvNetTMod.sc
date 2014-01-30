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
                        (sig <@ tEventSource.postln).collect{ |v| IO{
                            unit.items.do{ |u,i|
                                u.mapSet(key, v[i])
                            }
                        } }.reactimate
                        },{ |sig|
                            (sig <@ tEventSource.postln).collect{ |v| IO{
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
	var <sliderProxys;

	asUModFor { |unit|
        var tESM;
        timer = ENTimer(desc.delta);
		sliderProxys = desc.sliderValues.collect{ |v| FRPGUIProxy(nil, v) };
        tESM = timer.asENInput;
        tES = tESM.a;
		eventNetwork = EventNetwork( desc.createDesc(unit, tESM, sliderProxys.collect(_.asENInput) ) );
        eventNetwork.actuateNow;
    }

	sliderWindow {
		var sliders = desc.sliderValues.collect{ |v| Slider().value_(v) };
		var ezsliders = [sliders, desc.sliderLabels, ].flopWith{ |sl, label, val|
			VLayout( StaticText().string_(label), sl )
		};
		[sliders, sliderProxys].flopWith{ |sl, proxy| proxy.view_(sl) };
		^Window().layout_(HLayout(*ezsliders)).front
	}
}

ImmDef : MUENTModDef {
	classvar <currentSurface;
	classvar <currentTimeES;
	var <surface;
	var <sliderLabels, <sliderValues, <sliderSpecs, <slidersPresets;
	/*
	it's easier to enter the values with this notation
	slidersDescArg = [ "slider1", 0.1, "slider"2, 0.2, "slider3", 0.7 ]

	internally gets saved as:
	sliderLabels = [ "slider1", "slider"2, "slider3" ]
	sliderValues = [ 0.1, 0.2, 0.7 ]

	slidersPresets = [ [1.0,0.5,0.2], [0.4,0.2,0.1] ]
	*/

	*new { |descFunc, surface = (PSurface.geodesicSphere), delta = 0.1,
		slidersDescArg = #[], sliderSpecs=#[], slidersPresets=#[] |
		var check1 = if(slidersDescArg.size.odd){ Error("ImmDef - slidersDescArg: array size must be even").throw };
		var x = slidersDescArg.clump(2).flop;
		var sliderLabels = x[0] ? [];
		var sliderValues = x[1] ? [];
		var sliderSpecs2 = sliderSpecs.collect(_.asSpec);
		this.checkArgs(\ImmDef, \new, [descFunc, surface, delta, sliderLabels,
			sliderValues, sliderSpecs2, slidersPresets],
			[Function, PSurface, SimpleNumber, Array, Array, Array, Array] );
        ^super.newCopyArgs(descFunc, delta, surface, sliderLabels, sliderValues, sliderSpecs2, slidersPresets)
    }

    createDesc { |unit, tESM, slidersM|
		this.checkArgs(\ImmDef, \createDesc, [unit, tESM], [MU, Writer] );
        ^tESM >>= { |tEventSource|
            var tSignal = tEventSource.hold(0.0);
			currentSurface = surface;
			currentTimeES = tEventSource;
			slidersM.sequence(EventNetwork) >>= { |sliderSigs|
				var mappedSigs = [sliderSigs, sliderSpecs].flopWith{ |sig, spec|
					sig.collect{ |v| spec.map(v) }
				};
				ENDef.evaluate( descFunc, [tSignal]++mappedSigs )
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
		^[descFunc, surface, delta,  [sliderLabels, sliderValues].flop.flatten, sliderSpecs, slidersPresets]
    }

}




















