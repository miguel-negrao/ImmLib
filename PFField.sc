/*
TO-DO

function composition ?




*/

PField {
    var <func;

    *new{ |f|  // f = { |p,t, c1, c2, ...| ...}

        ^super.newCopyArgs(f)
    }

    //args should all be FPSignals, sequence needs Applicative instance.
    /*
    if we just used normal function evaluation:

    f = { |p,t,k1,k2| ... }

    what I would like to do

    g <‰> t <*> sl1 <*> sl2;

    g = { |t,k1,k2| surface.points.collect{ |p| f.(p,t,k1,k2) }

    create g from f and surface

    g = { |...args| surface.points.collect{ |p| f.( *args.prependI(p) ) } }

    would this be easier or less easy ?
    */
    value{ |surface...args|

        var f = { |args2|
            surface.points.collect{ |p| func.(*([p]++args2)) }
        };
        ^(f <%> args.sequence)
    }

    + { |aPfield|
        ^PField(this.func + aPfield.func)
    }

    - { |aPfield|
        ^PField(this.func - aPfield.func)
    }

    * { |aPfield|
        ^PField(this.func * aPfield.func)
    }

    / { |aPfield|
        ^PField(this.func / aPfield.func)
    }

    test { |...specs|
        var plot = ParameterFieldPlot2(\sphere, "" );
        ^if(specs.size > 0 ) {
            var sliders = specs.collect{ LayoutSlider("") };
            var plot = ParameterFieldPlot2(\sphere, "" );

            var w = Window().layout_(
                VLayout(* sliders.collect(_[0]) )
            );

            var descFunc = { |t|
                [sliders,specs].flopWith{|x,spec|
                    x[1].asENInput.fmap{ |es| es.linlin(0.0,1.0,spec[0],spec[1]) }
                }.sequence( Writer( _, Tuple3([  ], [  ], [ ]) ) ).postln >>= { |slevs|
                    plot.animateOnly(* ( [this,t]++slevs) )
                }.postln
            };

            //"sliders are : %".format(sliders).postln;
            //"specs are : %".format(spec).postln;

            MUAnimatedInteraction(descFunc, 0.1).test >>= { |n|
                n.actuate
            } >>=| plot.startRendererIO >>=| w.frontIO >>=| IO{
                CmdPeriod.doOnce({
                    w.close;
                })
            };


        } {
            MUAnimatedInteraction({ |t| plot.animateOnly(this,t) }, 0.1).test >>= { |n|
                n.actuate
            } >>=| plot.startRendererIO >>=| IO{
                CmdPeriod.doOnce({
                    plot.stopRenderer;
                })
            };
        }
    }

    //bulti-in functions
    *spotlight{ |centerPoint|
        ^PField( this.spotlightFunc( centerPoint ) )
    }

    *spotlightFunc{ |centerPoint|
        ^{ |p, t, c, d=0.2|
            var dist = PFFuncs.geodesicDist(centerPoint.theta, centerPoint.phi).(p.theta,p.phi);
            var c2 = c.linlin(0.0,1.0, d.neg, 1.0);
            var cpi = c2*pi;
            var cpid = cpi+d;
            if( dist < cpi ) {
                1
            } {
                if( dist < cpid ) {
                    PFFuncs.bump( dist.linlin(cpi,cpid,0.0,1.0) )
                } {
                    0
                }
            }
        }
    }

    *spotlightFunc2{
        ^{ |p, t, centerPoint, c, d=0.2|
            var dist = PFFuncs.geodesicDist(centerPoint.theta, centerPoint.phi).(p.theta,p.phi);
            var cpi = c*pi;
            var cpid = cpi+d;
            if( dist < cpi ) {
                1
            } {
                if( dist < cpid ) {
                    PFFuncs.bump( dist.linlin(cpi,cpid,0.0,1.0) )
                } {
                    0
                }
            }
        }
    }

    *barFunc {
        ^{ |p, t, widnessAngle|
            if(p.phi.abs < widnessAngle) {
                1.0
            } {
                0.0
            }
        }
    }

}





PFieldDef {
    classvar <all;
    var <key, <func;

    *new{ |key, f|  // f = { |p,t, c1, c2, ...| ...}

        var obj = super.newCopyArgs(key, f);
        if(all.isNil) { all = IdentityDictionary.new };
        all.put(key, obj);
        ^obj
    }

    *value{ |key,surface...args| //args should all be FPSignals or EventStreams
        var f = { |args2|
            surface.points.collect{ |p|
                all.at(key) !? { |pfdef| pfdef.func.(*([p]++args2)) } ?? { 0 }
            }
        };
        ^f <%> args.sequence;
    }
}












//to delete
PFNetwork {

    var <netDesc;

    *new{ arg func ...args;
        var timer = EventNetwork.newTimer.asWriterReader;
        var f = { |theargs, bindargs|
            theargs.match({
                func.(*bindargs.asArray).asWriterReader
                },{ |onearg,rest|
                    onearg >>= { |bindvar| f.(rest, bindargs.add(bindvar) ) }
            })
        };
        ^super.newCopyArgs( f.( timer %% LazyList.fromArray(args.collect(_.asWriterReader) ), LazyListEmpty ) );
    }


}

//to archive
PFieldOld1 {
    var <func;

    *new{ |f| // f = { |p,t, c1, c2, ...| ...}
        ^super.newCopyArgs(f)
    }

    value{ |...args|
        ^WriterReader( { |points|
            var f;
            " points are %".format(points).postln;
            f = { |args2| points.collect{ |p| func.(*([p]++args2)) } };
            Tuple2( f <%> args.sequence, Tuple2([],[]) );
        } )
    }
}

//to archive
PFieldOld2 {
    var <func;

    *new{ |f| // f = { |p,t, c1, c2, ...| ...}
        ^super.newCopyArgs(f)
    }

    value{ |...args|
        var f = { |args2| Reader( { |points| points.collect{ |p| func.(*([p]++args2)) } } ) };
        ^f <%> args.sequence
    }
}

//to delete
PFNetwork3 {

    *new{ arg func ...args;
        var argsEN = args.collect(_.asENInput).asLazy;
        var timer = EventNetwork.newTimer.collect{ |x| x.hold(0.0) };
        var f = { |theargs, bindargs|
            theargs.match({
                func.(*bindargs.asArray)
                },{ |onearg,rest|
                    onearg >>= { |bindvar| f.(rest, bindargs.add(bindvar) ) }
            })
        };
        var desc = f.( timer %% argsEN, LazyListEmpty );
        //var desc = func <%%> (timer %% args.asLazy);
        ^EventNetwork( desc );
    }


}
//just temporary hack, to be deleted
PFNetwork4 {

    *new{ arg func,delta=0.1 ...args;
        var argsEN = args.collect(_.asENInput).asLazy;
        var timer = EventNetwork.newTimer(delta).collect{ |x| x.hold(0.0) };
        var f = { |theargs, bindargs|
            theargs.match({
                func.(*bindargs.asArray)
                },{ |onearg,rest|
                    onearg >>= { |bindvar| f.(rest, bindargs.add(bindvar) ) }
            })
        };
        var desc = f.( timer %% argsEN, LazyListEmpty );
        //var desc = func <%%> (timer %% args.asLazy);
        ^EventNetwork( desc );
    }


}

TestFunc : Function{

    *new{ |f|
        ^super.newCopyArgs(*f.getArgs)
    }

    test123{
        "test123".postln;
    }
}

+ Function {

    getArgs{
        ^[def, context]
    }
}
