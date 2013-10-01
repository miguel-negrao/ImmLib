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

    *new { |descFunc, surface, delta = 0.1|
        ^super.newCopyArgs(descFunc, delta, surface)
    }

    createDesc { |unit, tESM|
        ^tESM >>= { |tEventSource|
            var tSignal = tEventSource.hold(0.0);
			currentSurface = surface;
			ENDef.evaluate( descFunc, [tSignal] )
            >>= this.addReactimatesFunc(unit, tEventSource)
        }
    }

}




















