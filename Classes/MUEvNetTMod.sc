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

ImmDef : MUENTModDef {
	classvar <currentSurface;
	var <surface;

	*new { |descFunc, surface = (PSurface.geodesicSphere), delta = 0.1|
		this.checkArgs(\ImmDef, \new, [descFunc, surface, delta], [Function, PSurface, SimpleNumber] );
        ^super.newCopyArgs(descFunc, delta, surface)
    }

    createDesc { |unit, tESM|
		//this.checkArgs( [unit, tESM], [Unit, ] );
		this.checkArgs(\ImmDef, \createDesc, [unit, tESM], [MU, Writer] );
        ^tESM >>= { |tEventSource|
            var tSignal = tEventSource.hold(0.0);
			currentSurface = surface;
			ENDef.evaluate( descFunc, [tSignal] )
            >>= this.addReactimatesFunc(unit, tEventSource)
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



}




















