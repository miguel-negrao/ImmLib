/*

(
x = PField{ |p,t,c| t + c };
y = PField{ |p,t,c| t * c };
z = x + y
)

p = PSurface.sphere(1)
z.value(p, Var(2.0), 10.0 ).do(postln(_))


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

    *new{ |f| // f = { |u, v, t, c1, c2, ...| ...}
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
		var points, f;

		//runtime type checking
		if( surface.isKindOf(PSurface).not ) {
			Error("PField - first argument must be of class PSurface").throw
		};
		if( (args.size == 0) or: { args[0].isKindOf(FPSignal).not } ) {
			Error("PField - second arg must be a time signal").throw
		};
		//only accepting numbers for the moment
		args.do{ |x|
			if( (x.isKindOf(SimpleNumber) || x.isKindOf(FPSignal) ).not ) {
				Error("PField - arguments must be either SimpleNumbers or FPSignals").throw }
		};

		points = surface.points;
		f = { |args2|
			points.collect{ |xs| func.value( *(xs++args2) ) }
		};
		^(f <%> args.collect(_.asFPSignal).sequence)

    }

    valueArray{ |...allargs|
		var last, newArgs, surface, args, points, f;

		//runtime type checking
		if( allargs.size < 2 ) {
			Error("PField - must have at least two arguments: the surface and the time signal").throw
		};
		if( allargs[0].isKindOf(PSurface).not ) {
			Error("PField - first argument must be of class PSurface").throw
		};
		if( allargs[1].isKindOf(FPSignal).not ) {
			Error("PField - second arg must be a time signal").throw
		};

		//if last element is a list append to the rest of the list
		//why did I add this ??
		last = allargs.last;
		newArgs = if( last.isKindOf(Array) || last.isKindOf(List) ) {
			allargs[..(allargs.size-2)]++last
		} {
			allargs
		};
		surface = newArgs[0];
		args = newArgs[1..];

		//only accepting numbers for the moment
		args.do{ |x|
			if( (x.isKindOf(SimpleNumber) || x.isKindOf(FPSignal) ).not ) {
				Error("PField - arguments must be either SimpleNumbers or FPSignals").throw }
		};

		points = surface.points;
		f = { |args2|
			points.collect{ |xs| func.value( *(xs++args2) ) }
		};
		^(f <%> args.collect(_.asFPSignal).sequence)
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

	plotImage{ |surface, hsize=10, contrast=1, color=#[247,145,30], t=0...args|

		var min, max, image, rgbs, valArray, xmap, ymap, vsize, w;

		color = color ? 122;
		min = 100;
		max = 0;
		vsize = (hsize*surface.dv/surface.du).asInteger;

		hsize = hsize.asInteger;
		vsize = vsize.asInteger;

		image = Image.new(hsize,vsize);
		image.interpolation = 'smooth';
		//image.accelerated_(true);

		xmap = { |x| x.linlin(0, hsize-1, surface.rangeU[0], surface.rangeU[1]) };
		ymap = { |y| y.linlin(0, vsize-1, surface.rangeV[0], surface.rangeV[1])};

		w = image.plot(freeOnClose:true, showInfo:false);

		fork{
			t = 0;
			inf.do{
				valArray = Array.fill(hsize*vsize, { |i|
			var x = i%hsize, y = i.div(hsize);
					this.func.valueArray([xmap.(x),ymap.(y),t]++args).abs;
		});

				{image.pixels_(
					Int32Array.fill(hsize*vsize, { |i|
						var val;
						val = valArray[i]*contrast; //- ((1-contrast)/2);
						val = (color*val).floor.asInteger.clip(0,255);
						Integer.fromRGBA(val[0],val[1],val[2],255);

					})
				);
				w.refresh;
				"beep".postln;
				}.defer;
				0.1.wait;
				t = t + 0.1;
			}
		}

	}

	//I don't think this is working currentl
	/*
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
    }*/

	/*test2 {
		^MUENTModDef.test({ |tSig|
			PFieldPlot.animateOnly(this, tsig)

		}, 0.1)
	}*/

    //bulti-in functions
	//for fixed rotations I could rotate the points before using them
	rotate { |pf| //angle1, 2, 3 -pi/2, pi/2
		^PField{ |u, v, t, rotate=0, tilt=0, tumble=0...args|
			var newP = UnitSpherical(u,v).asCartesian.rotate(tilt).tilt(tilt).tumble(tumble).asSpherical;
			this.func.valueArray( [newP.theta, newP.phi, t]++args )
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

    *spotlightFixed{ |surface, u, v|
        ^PField( this.spotlightFixedFunc( surface, u, v ) )
    }

    *spotlightFixedFunc{ |surface, u2, v2| //UnitSpherical
		var bump = this.prBump;
		var distFunc = surface.distFunc;
		var maxDist = surface.maxDist;

        ^{ |u1, v1, t, c, d=0.2|
            var dist = distFunc.(u1, v1, u2, v2);
            var c2 = c.linlin(0.0,1.0, d.neg, 1.0);
            var cpi = c2*maxDist;
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

	*spotlight{ |surface|
		^PField( this.spotlightFunc(surface) )
	}

	*spotlightFunc{ |surface|
		var bump = this.prBump;
		var distFunc = surface.distFunc;
		var maxDist = surface.maxDist;

		^{ |u1, v1, t, u2, v2, c, d=0.2|
			var dist = distFunc.(u1, v1, u2, v2);
			var c2 = c.linlin(0.0,1.0, d.neg, 1.0);
			var cpi = c2 * maxDist;
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

	*barU{ |surface|
		^PField( this.barFuncU( surface ) )
	}

    *barFuncU { |surface|
		var d = surface.du/2;
        ^{ |u, v, t, widness|
			if( (surface.ucenter - u).abs < (widness * d) ) {
                1.0
            } {
                0.0
            }
        }
    }

	*barV{ |surface|
		^PField( this.barFuncV( surface ) )
	}

    *barFuncV { |surface|
		var d = surface.dv/2;
        ^{ |u, v, t, widness|
			if( (surface.vcenter - v).abs < (widness * d) ) {
                1.0
            } {
                0.0
            }
        }
    }

	*expandContract{ |surface|
		^PField( this.expandContractFunc(surface) )
	}

	//this is kind of unique to closed surfaces.
	*expandContractFunc { |surface|
		var scale = { |x| var k = 15; k**x/k };
		var f = PField.spotlightFunc(surface);
		^{|u1, v1, t, u2, v2, c|
			if(c < 0.5){
				f.(u1, v1, t, u2, v2, scale.( (c*2) ) )
			} {
				f.(u1, v1, t, u2+pi, v2.neg, scale.( c.linlin(0.5,1.0, 1.0, 0.0) ) )
			}
		}
	}

	*expandContract2{ |surface|
		^PField( this.expandContract2Func(surface) )
	}

	//this is kind of unique to closed surfaces.
	*expandContract2Func { |surface|
		var scale = { |x| var k = 15; k**x/k };
		var f = PField.spotlightFunc(surface);
		^{|u1, v1, t, u2, v2, u3, v3, c|
			if(c < 0.5){
				f.(u1, v1, t, u2, v2, scale.( (c*2) ) )
			} {
				f.(u1, v1, t, u3, v3, scale.( c.linlin(0.5,1.0, 1.0, 0.0) ) )
			}
		}
	}

	*gradient { |surface|
		^PField( this.gradientFunc( surface ) )
	}

	*gradientFunc { |surface|
		var distFunc = surface.distFunc;
		var maxDist = surface.maxDist;
		^{ |u, v, t, u2, v2, a, b|
			var x = distFunc.(u, v, u2, v2)/maxDist;
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
		^PField({ |u, v, t,f=3|
			(shfunc.( u, (pi/2)-v)*cos(f*t)).linlin(-1.0,1.0,0.0,1.0)
		})
	}

	//reactive fields (i.e. using FRP switching)
	//Random Hills
	*generateHillsFunc {
		^{ |n, s, sizeA=0.3, sizeB=0.5, bumpSize=0.5| n.collect{
			var tau = 2*pi;
			var u2 = rrand(s.rangeU[0], s.rangeU[1]);
			var v2 = rrand(s.rangeV[0], s.rangeV[1]);
			var size = rrand(sizeA, sizeB);
			{ |u,v,t|
				//PFFuncs.growArea(UnitSpherical(theta, phi)).(p,size,0.5)
				PField.spotlightFixedFunc(s, u2, v2).(u,v,nil,size, bumpSize)
			}
		}.sum / n
		}
	}

	//Random Hills bipolar
	*generateHillsBipolarFunc {
		^{ |n, s, sizeA=0.3, sizeB=0.5, bumpSize=0.5| (n.collect{
			var tau = 2*pi;
			var u2 = rrand(s.rangeU[0], s.rangeU[1]);
			var v2 = rrand(s.rangeV[0], s.rangeV[1]);
			var size = rrand(sizeA, sizeB);
			var sig = [-1,1].choose;
			{ |u,v,t|
				PField.spotlightFixedFunc(s, u2, v2).(u,v,nil,size, bumpSize) * sig
			}
		}.sum / n)*0.5 + 0.5
		}
	}

	*randomPatchGeneral { |generateHillsFunc, surface, t, numSecs, numHills = 5, sizeA=0.3, sizeB=0.5, bumpSize = 0.5|
		//time wrapping around numSecs
		var gen = { generateHillsFunc.(numHills, surface, sizeA, sizeB, bumpSize) };
		var t2 = t.collect(_.mod(numSecs));
		var initFs = T( gen.(), gen.() );

		//this generates an event every numSecs containing the from and to functions
		//to be morphed
		var changefuncEvent = t2
		.changes
		.storePrevious
		.select{ |tup| (tup.at2 < 0.2) && (tup.at1 > (numSecs-0.2) ) }
		.hold(0.0).inject(initFs,{ |state,x|
			T( state.at2, gen.() );
		});

		//this morphs from function A to function B
		var f = { |tup|
			PField({ |u, v, t|
				var k = 0.5;
				var t2 = t/numSecs;
				( (1-t2) * tup.at1.(u,v)  ) + (t2 * tup.at2.(u,v))
			}).(surface,t2)
		};

		//event switching
		^changefuncEvent >>= f
	}

	*randomHills { |surface, t, numSecs, numHills = 5, sizeA=0.3, sizeB=0.5, bumpSize = 0.5|
		^this.randomPatchGeneral( this.generateHillsFunc, surface, t, numSecs, numHills, sizeA, sizeB, bumpSize )
	}

	*randomHillsBipolar {|surface, t, numSecs, numHills = 5, sizeA=0.3, sizeB=0.5, bumpSize = 0.5|
		^this.randomPatchGeneral( this.generateHillsBipolarFunc, surface, t, numSecs, numHills, sizeA, sizeB, bumpSize )
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

	//start pfielda after n seconds switch to pfield bi


}




/*
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
*/










