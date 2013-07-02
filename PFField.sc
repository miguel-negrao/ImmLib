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

	<> { |pf|
		^PField(func <> pf.func)
	}

	//I don't think this is working currently
    test { |...specs|
        var plot = Param(\sphere, "" );
        ^if(specs.size > 0 ) {
            var sliders = specs.collect{ LayoutSlider("") };
            var plot = PFieldPlot(\sphere, "Test PField" );

            var w = Window().layout_(
                VLayout(* sliders.collect(_[0]) )
            );

            var descFunc = { |t|
                [sliders,specs].flopWith{|x,spec|
                    x[1].asENInput.collect{ |es| es.linlin(0.0,1.0,spec[0],spec[1]) }
                }.sequence( Writer( _, Tuple3([  ], [  ], [ ]) ) ).postln >>= { |slevs|
                    plot.animateOnly(* ( [this,t]++slevs) )
                }.postln
            };

            //"sliders are : %".format(sliders).postln;
            //"specs are : %".format(spec).postln;

            UEvNetTModDef(descFunc, 0.1).test >>= { |n|
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

	/*test2 {
		^MUENTModDef.test({ |tSig|
			PFieldPlot.animateOnly(this, tsig)

		}, 0.1)
	}*/

    //bulti-in functions
	rotate { |pf| //angle1, 2, 3 -pi/2, pi/2
		^PField{ |p,t, xz=0, yz=0, xy=0...args|
			var newP = p.rotateXZ(xz).rotateYZ(yz).rotateXY(xy);
			this.func.valueArray( [newP,t]++args )
		}
	}
	*prBump{ ^{ |x| 2**((1-x.squared).reciprocal.neg)*2 } }

	*prGeodesicDist { |theta1, phi1|
		^{ | theta2, phi2|
			acos( cos(phi1)*cos(phi2)*cos(theta1-theta2) + (sin(phi1)*sin(phi2) ) )
		}
	}

	*prGeodesicDist2 {
		^{ |theta1, phi1, theta2, phi2|
			acos( cos(phi1)*cos(phi2)*cos(theta1-theta2) + (sin(phi1)*sin(phi2) ) )
		}
	}

    *spotlightFixed{ |theta, phi|
        ^PField( this.spotlightFixedFunc( theta, phi ) )
    }

    *spotlightFixedFunc{ |theta, phi| //UnitSpherical
		var bump = this.prBump;
		var geodesicDist = this.prGeodesicDist( theta, phi);
        ^{ |p, t, c, d=0.2|
            var dist = geodesicDist.(p.theta,p.phi);
            var c2 = c.linlin(0.0,1.0, d.neg, 1.0);
            var cpi = c2*pi;
            var cpid = cpi+d;
            if( dist < cpi ) {
                1
            } {
                if( dist < cpid ) {
                    bump.( dist.linlin(cpi,cpid,0.0,1.0) )
                } {
                    0
                }
            }
        }
    }

	*spotlight{
		^PField( this.spotlightFunc )
	}

	*spotlightFunc{
		var bump = this.prBump;
		var geodesicDist2 = this.prGeodesicDist2;

		^{ |p, t, theta, phi, c, d=0.2|
			var dist = geodesicDist2.( p.theta, p.phi, theta, phi);
			var c2 = c.linlin(0.0,1.0, d.neg, 1.0);
			var cpi = c2*pi; //half of the perimeter of a unit circle measures pi
			var cpid = cpi+d;
			if( dist < cpi ) {
				1
			} {
				if( dist < cpid ) {
					bump.( dist.linlin(cpi,cpid,0.0,1.0) )
				} {
					0
				}
			}
		}

	}

	*bar{
		^PField( this.barFunc )
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

	*expandContract{
		^PField( this.expandContractFunc )
	}

	*expandContractFunc {
		var scale = { |x| var k = 15; k**x/k };
		^{|p, t, theta, phi, c|
			if(c < 0.5){
				PField.spotlightFunc.(p, t, theta, phi, scale.( (c*2) ) )
			} {
				PField.spotlightFunc.(p, t, theta+pi, phi.neg, scale.( c.linlin(0.5,1.0, 1.0, 0.0) ) )
			}
		}
	}

	*gradient {
		^PField( this.gradientFunc )
	}

	*gradientFunc {
		var distFunc = this.prGeodesicDist2;
		^{ |p, t, theta, phi, a, b|
			var x = distFunc.(theta, phi, p.theta,p.phi)/pi;
			(a*(1-x)) + (b*x)
		}
	}

	//double factorial
	*sphericalHarmonic{ |m,l|
		var dfact = { |x| if(x <= 0) { 1 } { dfact.(x-2) * x } };
		//Legendre polynomials
		var legendrepol = { |m,l|
			case
			{m>l} { Error("Error, m>l").throw }
			{l==m } { { |x|  ((-1)**m)*dfact.(2*m-1)*((1-x.squared)**(m/2)) } }
			{l==(m+1)} { ( _*(2*m+1))*legendrepol.(m,m) }
			{l>=(m+2) } {  ((_*(2*l-1))*legendrepol.(m,l-1)-((l+m-1)*legendrepol.(m,l-2)))/(l-m) }
		};
		var simplifiedsh  = { |m,l|
			if ( (m>l) || (m<l.neg) ) {  Error("error m< -l or m>l").throw };
			case
			{m>0}{ { |phi,theta|  legendrepol.(m.abs,l).(cos(theta))*cos(m*phi) } }
			{m == 0}{ { |phi,theta| legendrepol.(0,l).(cos(theta)) } }
			{m <0 }{ { |phi,theta| legendrepol.(m.abs,l).(cos(theta))*sin(m.abs*phi) } }
		};

		var shfunc = simplifiedsh.(m,l);
		^PField({ |p,t,f=3|
			(shfunc.( p.theta, (pi/2)-p.phi)*cos(f*t)).linlin(-1.0,1.0,0.0,1.0)
		})
	}

	//Random Hills
	*generateHillsFunc {
		^{ |n| n.collect{
			var tau = 2*pi;
			var theta = rrand(0.0,tau);
			var phi = rrand(tau.neg/4,tau/4);
			var size = rrand(0.3,0.5);
			{ |p|
				PFFuncs.growArea(UnitSpherical(theta, phi)).(p,size,0.5)
			}
		}.sum / n
		}
	}

	*generateHillsBipolarFunc {
		^{ |n| (n.collect{
			var tau = 2*pi;
			var theta = rrand(0.0,tau);
			var phi = rrand(tau.neg/4,tau/4);
			var size = rrand(0.3,0.5);
			{ |p|
				PFFuncs.growArea(UnitSpherical(theta, phi)).(p,size,0.5) * [-1,1].choose
			}
		}.sum / n)*0.5 + 0.5
		}
	}

	*randomPatchGeneral { |generateHillsFunc, surface, t, numSecs, numHills = 5|
		//time wrapping around numSecs
		var t2 = t.collect(_.mod(numSecs));
		var initFs = T( generateHillsFunc.(5), generateHillsFunc.(5) );

		//this generates an event every numSecs containing the from and to functions
		//to be morphed
		var changefuncEvent = t2
		.changes
		.storePrevious
		.select{ |tup| (tup.at2 < 0.2) && (tup.at1 > (numSecs-0.2) ) }
		.hold(0.0).inject(initFs,{ |state,x|
			T( state.at2, generateHillsFunc.(numHills));
		});

		//this morphs from function A to function B
		var f = { |tup|
			PField({ |p, t|
				var k = 0.5;
				var t2 = t/numSecs;
				( (1-t2) * tup.at1.(p)  ) + (t2 * tup.at2.(p))
			}).(surface,t2)
		};

		//event switching
		^changefuncEvent >>= f
	}

	*randomHills { |surface, t, numSecs, numHills = 5|
		^this.randomPatchGeneral( this.generateHillsFunc, surface, t, numSecs, numHills )
	}

	*randomHillsBipolar { |surface, t, numSecs, numHills = 5|
		^this.randomPatchGeneral( this.generateHillsBipolarFunc, surface, t, numSecs, numHills )
	}

	*continousRandomSpotlight{ |surface, t, numSecs|
		//time wrapping around numSecs
		var t2 = t.collect{ |t| t.mod(numSecs) / numSecs };
		var randomSpotLight = {
			PField.spotlightFixedFunc( rrand(0, 2pi), rrand(pi.neg,pi) )
		};

		var changefuncEvent = t2
		.changes
		.storePrevious
		.select{ |tup| (tup.at2 < 0.2) && (tup.at1 > (0.8) ) }
		.hold(0.0).collect( randomSpotLight );

		var f = { |h|
			PField({ |p, t|
				if(t < 0.5) {
					h.(p,0,1-(t*2),0.2)
				} {
					h.(p,0,(t-0.5)*2,0.2)
				}
			}).(surface,t2)
		};

		//event switching
		^changefuncEvent >>= f
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
