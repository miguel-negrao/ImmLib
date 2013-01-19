/*

(
x = PField{ |p,t,c| t + c };
y = PField{ |p,t,c| t * c };
z = x + y
)

p = ParameterSurface.sphere(1)
z.value(p, Var(2.0), Var(10.0) ).do(postln(_))


(
x = PField{ |p,t,c| t + c }.linlin(0.0,1.0,0.0,10.0);
x.value(p, Var(0.1), Var(0.3) ).do(postln(_))
)

(
x = PField{ |p,t,c| t + c }.linlin(0.0,1.0,0.0,10.0);
x.valueArray( [p, Var(0.1), Var(0.3)] ).do(postln(_))
)

(
x = PField{ |p,t,c| t + c };
p = ParameterSurface.sphere(2);
x.valueArray( p, [ Var(0.1), Var(0.3) ] ).do(postln(_))
)


*/

PField : AbstractFunction {
    var <func;

    *new{ |f| // f = { |p,t, c1, c2, ...| ...}
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

    Perhaps, assum that PField operates on signals and lift all the operations into signals.

    [1,2][..0]

    */
    value{ |surface...args|
        if(surface.notNil) {
            var f = { |args2|
                surface.points.collect{ |p| func.value(*([p]++args2)) }
            };
            ^(f <%> args.sequence)
        } {
            Error("PField must have at least one argument (the surface)").throw
        }
    }

    valueArray{ |...allargs|
        if(allargs.size > 0) {
            var last = allargs.last;
            var newArgs = if( last.isKindOf(Array) || last.isKindOf(List) ) {
                allargs[..(allargs.size-2)]++last
            } {
                allargs
            };
            var surface = newArgs[0];
            var args = newArgs[1..];
            var f = { |args2|
                surface.points.collect{ |p| func.value(*([p]++args2)) }
            };
            if( surface.isKindOf( ParameterSurface ).not ) {
                Error(" First argument of PField must be a ParameterSurface ").throw
            };
            ^(f <%> args.sequence)
        } {
            Error("PField must have at least one argument (the surface)").throw
        };

    }

    // override these in subclasses to perform different kinds of function compositions
    composeUnaryOp { arg aSelector;
        ^PField( func.composeUnaryOp( aSelector ) )
    }
    composeBinaryOp { arg aSelector, something, adverb;
        ^PField( func.composeBinaryOp(aSelector, something.func, adverb) )
    }
    reverseComposeBinaryOp { arg aSelector, something, adverb;
        ^PField( func.reverseComposeBinaryOp(aSelector, something.func, adverb) )
    }
    composeNAryOp { arg aSelector, anArgList;
        ^PField( func.composeNAryOp(aSelector, anArgList ) )
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
        var theta = centerPoint.theta;
        var phi = centerPoint.phi;
        ^{ |p, t, c, d=0.2|
            var dist = PFFuncs.geodesicDist(theta, phi).(p.theta,p.phi);
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

    *test123{
        ^{ "someone evaluated this !!".postln; }
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
