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
	valueS{ |surface...args|
		var points, f;

		//runtime type checking
		if( surface.isKindOf(PSurface).not ) {
			Error("PField - first argument must be of class PSurface").throw
		};
		if( (args.size == 0) or: { args[0].isKindOf(FPSignal).not } ) {
			Error("PField - second arg must be a time signal - "++if(args.size!=0){ "got instead %".format(args[0]) }{"didn't receive second argument"} ).throw
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

	value{ |...args|
		^this.valueS(*[ImmDef.currentSurface]++args)
	}

	//to make it easy to turn on and off animation
	animate{ |...args|
		^PSmoothPlot(*args.prependI(this))
	}

	valuePlot{ |...args|
		^PSmoothPlot(*[this]++args)
	}

	valueArrayS{ |...allargs|
		//valueArray can receive some or all of the args as list
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

	valueArray{ |...args|
		^this.valueArrayS(*[ImmDef.currentSurface]++args)
	}

	// override these in subclasses to perform different kinds of function compositions
	composeUnaryOp { arg aSelector;
		^PField( func.composeUnaryOp( aSelector ) )
	}
	composeBinaryOp { arg aSelector, something, adverb;
		var x = if(something.isKindOf(PField)){ something.func }{ something };
		^PField( func.composeBinaryOp(aSelector, x, adverb) )
	}
	reverseComposeBinaryOp { arg aSelector, something, adverb;
		var x = if(something.isKindOf(PField)){ something.func }{ something };
		^PField( func.reverseComposeBinaryOp(aSelector, x, adverb) )
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
	rotate { //angle1, 2, 3 -pi/2, pi/2
		^PField{ |u, v, t, rotate=0, tilt=0, tumble=0...args|
			var newP = UnitSpherical(u,v).asCartesian.rotate(rotate).tilt(tilt).tumble(tumble).asSpherical;
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

	*spotlightFixed{ | u, v|
		^PField( this.spotlightFixedFunc( ImmDef.currentSurface, u, v ) )
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

	*spotlight{
		^PField( this.spotlightFunc( ImmDef.currentSurface ) )
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

	*barU{
		^PField( this.barFuncU( ImmDef.currentSurface ) )
	}

	*barFuncU { |surface|
		var d = surface.du;
		^{ |u, v, t, wideness|
			if( u < (wideness * d) ) {
				1.0
			} {
				0.0
			}
		}
	}

	*barV{
		^PField( this.barFuncV( ImmDef.currentSurface ) )
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

	*expandContract{
		^PField( this.expandContractFunc(ImmDef.currentSurface) )
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

	//temp
	*expandContract3{
		^PField( this.expandContractFunc3(ImmDef.currentSurface) )
	}

	//this is kind of unique to closed surfaces.
	*expandContractFunc3 { |surface|
		var f = PField.spotlightFunc(surface);
		^{|u1, v1, t, u2, v2, c|
			if(c < 0.5){
				f.(u1, v1, t, u2, v2, (c*2) )
			} {
				f.(u1, v1, t, u2+pi, v2.neg, c.linlin(0.5,1.0, 1.0, 0.0) )
			}
		}
	}
	//temp

	*expandContract2{
		^PField( this.expandContract2Func( ImmDef.currentSurface ) )
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

	*gradient {
		^PField( this.gradientFunc( ImmDef.currentSurface ) )
	}

	*gradientFunc { |surface|
		var distFunc = surface.distFunc;
		var maxDist = surface.maxDist;
		^{ |u, v, t, u2, v2, a, b|
			var x = distFunc.(u, v, u2, v2)/maxDist;
			(a*(1-x)) + (b*x)
		}
	}

	*gradientC {
		^PField( this.gradientCFunc( ImmDef.currentSurface ) )
	}

	*gradientCFunc { |surface|
		var distFunc = surface.distFunc;
		var maxDist = surface.maxDist;
		^{ |u, v, t, u2, v2, a, b, curve|
			var x = distFunc.(u, v, u2, v2)/maxDist;
			if( curve.abs < 0.0001) {
				(a*(1-x)) + (b*x)
			} {
				var denom = 1.0 - exp(curve);
				var  numer = 1.0 - exp(x * curve);
				a + ((b - a) * (numer/denom))
			}
		}
	}

	*gradientF { |f|
		^PField( this.gradientFFunc( ImmDef.currentSurface, f ) )
	}

	*gradientFFunc { |surface, f|
		var distFunc = surface.distFunc;
		var maxDist = surface.maxDist;
		^{ |u, v, t, u2, v2, a, b|
			var x = distFunc.(u, v, u2, v2)/maxDist;
			f.(x, a, b)
		}
	}

	*gradientEnv { |env|
		^PField( this.gradientEnvFunc( ImmDef.currentSurface, env ) )
	}

	*gradientEnvFunc { |surface, env|
		var distFunc = surface.distFunc;
		var maxDist = surface.maxDist;
		var array = env.asMultichannelArray[0];
		^{ |u, v, t, u2, v2, a, b, curve|
			var x = distFunc.(u, v, u2, v2)/maxDist;
			array.envAt(x).linlin(0.0,1.0,a,b)
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
		^{ |n, s, sizeA=0.3, sizeB=0.5, bumpSize=0.5, heightA=1.0, heightB=1.0|
			n.collect{
				var tau = 2*pi;
				var u2 = rrand(s.rangeU[0], s.rangeU[1]);
				var v2 = rrand(s.rangeV[0], s.rangeV[1]);
				var size = rrand(sizeA, sizeB);
				var height = rrand(heightA, heightB);
				var f = PField.spotlightFixedFunc(s, u2, v2);
				{ |u,v,t|
					//PFFuncs.growArea(UnitSpherical(theta, phi)).(p,size,0.5)
					f.(u,v,nil,size, bumpSize) * height
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
			var f = PField.spotlightFixedFunc(s, u2, v2);
			{ |u,v,t|
				f.(u,v,nil,size, bumpSize) * sig
			}
		}.sum / n)*0.5 + 0.5
		}
	}

	*randomPatchGeneral { |generateHillsFunc, surface, t, numSecs, numHills = 5, sizeA=0.3, sizeB=0.5, bumpSize = 0.5, heightA=1.0, heightB=1.0|
		var numSecsSig = numSecs.asFPSignal;
		var numHillsSig = numHills.asFPSignal;
		var sizeASig = sizeA.asFPSignal;
		var sizeBSig = sizeB.asFPSignal;
		var bumpSizeSig = bumpSize.asFPSignal;
		var heightASig = heightA.asFPSignal;
		var heightBSig = heightB.asFPSignal;

		//this morphs from function A to function B
		var f = { |xs|

			var oldState, n, nextnumHills, nextsizeA, nextsizeB, nextbumpSize, nexthA, nexthb, newState, a, b, localT, pfSig, r;
			#oldState, n, nextnumHills, nextsizeA, nextsizeB, nextbumpSize, nexthA, nexthb = xs;
			nextnumHills = nextnumHills.asInteger;
			//"Running switch function again realt: %".format(t.now).postln;
			//we create a new set of hills to morph to:
			newState = T( oldState.at2, generateHillsFunc.(nextnumHills, surface, nextsizeA, nextsizeB, nextbumpSize, nexthA, nexthb) );
			a = newState.at1;
			b = newState.at2;

			//we create a new local time signal starting from 0;
			localT = t.integral1;

			pfSig = PField({ |u, v, t|
				var t2 = t/n;
				( (1-t2) * a.(u,v)  ) + (t2 * b.(u,v))
			}).(localT);

			{ |x, t, nnumSecs, nnumHills, nsizeA, nsizeB, nbumpSize, nha, nhb|
				T(x, if((t>=n)){Some([newState, nnumSecs, nnumHills, nsizeA, nsizeB, nbumpSize, nha, nhb])}{None()}) }
			.lift.(pfSig, localT, numSecsSig, numHillsSig, sizeASig, sizeBSig, bumpSizeSig, heightASig, heightBSig);
		};

		//event switching
		//calling .now is not pure...
		var startValues = [numSecsSig, numHillsSig, sizeASig, sizeBSig, bumpSizeSig, heightASig, heightBSig].collect(_.now);
		var startValues2 = [startValues[1].asInteger, surface]++startValues[2..];
		^f.selfSwitch( [ T( generateHillsFunc.(*startValues2), generateHillsFunc.(*startValues2) ) ]++startValues );
	}

	*randomHills { | t, numSecs=5.0, numHills = 5, sizeA=0.3, sizeB=0.5, bumpSize = 0.5, heightA=1.0, heightB=1.0|
		//this.checkArgs(\PField, \randomHills,
			//[t, numSecs, numHills, sizeA, sizeB, bumpSize], [FPSignal]++(SimpleNumber ! 5));
		^this.randomPatchGeneral( this.generateHillsFunc, ImmDef.currentSurface, t,
			numSecs, numHills, sizeA, sizeB, bumpSize, heightA, heightB )
	}

	*randomHillsBipolar {| t, numSecs, numHills = 5, sizeA=0.3, sizeB=0.5, bumpSize = 0.5|
		//this.checkArgs(\PField, \randomHillsBipolar,
			//[t, numSecs, numHills, sizeA, sizeB, bumpSize], [FPSignal]++(SimpleNumber ! 5));
		^this.randomPatchGeneral( this.generateHillsBipolarFunc, ImmDef.currentSurface,
			t, numSecs, numHills, sizeA, sizeB, bumpSize )
	}

	*continousRandomSpotlight{ |t, numSecs|
		var check = this.checkArgs(\PField, \continousRandomSpotlight,
			[t, numSecs], [FPSignal, SimpleNumber] );
		var surface = ImmDef.currentSurface;
		//time wrapping around numSecs
		var t2 = t.collect{ |t| t.mod(numSecs) / numSecs };
		var randomSpotLight = {
			PField.spotlightFixedFunc( surface, rrand(0, 2pi), rrand(pi.neg,pi) )
		};

		var changefuncEvent = t2
		.changes
		.storePrevious
		.select{ |tup| (tup.at2 < 0.2) && (tup.at1 > (0.8) ) }
		.hold(0.0).collect( randomSpotLight );

		var f = { |h|
			PField({ |u, v, t|
				if(t < 0.5) {
					h.(u,v,0,1-(t*2),0.2)
				} {
					h.(u,v,0,(t-0.5)*2,0.2)
				}
			}).(t2)
		};

		//event switching
		^changefuncEvent.switch( f )
	}

	*moveHills { |t, numSecs, numHills = 5, size = 0.4, step = 0.4, startInSamePlace = true|

		var surface = ImmDef.currentSurface;
		var crossfade = { |x, a, b| ( a * (1-x) ) + (b * x) };
		var iteratePoints = { |ps, r=0.4| ps.collect{ |t| t |+| T( rrand( r.neg, r), rrand( r.neg, r) ) }};
		var genPoints = { |n| T( rrand(0, 2pi), rrand(-pi, pi) ) ! n };
		var genPoints2 = { |n| { T( rrand(0, 2pi), rrand(-pi, pi) ) } ! n };
		var newState = { |oldPoints, step = 0.4|  T( oldPoints, iteratePoints.(oldPoints, step) ) };

		//time wrapping around numSecs
		var t2 = t.collect(_.mod(numSecs));

		//Point :: T[Float, Float]
		//state :: T[ Point, Point ]
		var initFs = newState.( if(startInSamePlace) {
			genPoints
		} {
			genPoints2
		}.(numHills),
		step );

		//this generates an event every numSecs containing the from and to functions
		//to be morphed
		var changefuncEvent = t2
		.changes
		.storePrevious
		.select{ |tup| (tup.at2 < 0.2) && (tup.at1 > (numSecs-0.2) ) }
		.hold(0.0).inject( initFs, { |state,x|
			newState.( state.at2, step )
		});

		//this morphs from function A to function B
		var f = { |tup|

			t2.collect{ |time|

				var pos = crossfade.(time/numSecs, tup.at1, tup.at2);

				var funcs = pos.collect{ |p|
					PField.spotlightFixedFunc(surface, p.at1, p.at2)
				};

				surface.points.collect{ |v|
					funcs.collect{ |f|
						f.(v[0], v[1], t, size, 0.5)
					}.sum.min(1.0)
				}
			}
		};

		//event switching
		^changefuncEvent.switch( f )

	}



	//sin(2pi * f * y)
	//y = x + ct
	//sin(2pi * f* (x + ct) )
	//sin(2pi * ( (f*x) + (f*c*t) ) )
	//SinOsc.ar( f*c, f*x

	*waveUSin {  |t, l, freq, plot = false|

		var pf = PField({ |u,v, t, l|
			sin( 2pi * ( (l*u) + t) )
		});
		^if( plot ){
			PSmoothPlot(pf, t.changeRate(freq), l).postln
		} {
			pf.(t.changeRate(freq), l)
		}

	 }

	*waveVSin { |t, l, freq, plot = false|
		var pf = PField({ |u,v, t, l|
			sin( 2pi * ( (l*v) + t) )
		});
		^if( plot ){
			PSmoothPlot(pf, t.changeRate(freq), l)
		} {
			pf.(t.changeRate(freq), l)
		}
	}

	*wave2DSin { |t, u0, v0, l, freq, plot = false|
		var surface = ImmDef.currentSurface;
		var distFunc = surface.distFunc;
		var maxDist = surface.maxDist;
		var pf = PField({ |u,v, t, u0, v0, l|
			sin( 2pi * ((l*distFunc.(u,v,u0,v0)/maxDist) + t) )
		});
		^if( plot ){
			PSmoothPlot(pf, t.changeRate(freq), u0, v0, l)
		} {
			pf.(t.changeRate(freq), u0, v0, l)
		}
	}

	*wave2DSaw { |t, u0, v0, l, freq, plot = false|
		var surface = ImmDef.currentSurface;
		var distFunc = surface.distFunc;
		var maxDist = surface.maxDist;
		var pf = PField({ |u,v, t, u0, v0, l|
			( (l*distFunc.(u,v,u0,v0)/maxDist) + t) % 1.0
		});
		^if( plot ){
			PSmoothPlot(pf, t.changeRate(freq), u0, v0, l)
		} {
			pf.(t.changeRate(freq), u0, v0, l)
		}
	}

	*wave2D { |t, u0, v0, l, freq, g, plot = false|
		var surface = ImmDef.currentSurface;
		var distFunc = surface.distFunc;
		var maxDist = surface.maxDist;
		var pf = PField({ |u,v, t, u0, v0, l|
			g.( (l*distFunc.(u,v,u0,v0)/maxDist) + t)
		});
		^if( plot ){
			PSmoothPlot(pf, t.changeRate(freq), u0, v0, l)
		} {
			pf.(t.changeRate(freq), u0, v0, l)
		}
	}

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










