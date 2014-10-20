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

	*initClass {
		Class.initClassTree(Warp);
		ControlSpec.specs = ControlSpec.specs.addAll([
			\azimuth -> ControlSpec(0, 2pi,\lin,0,0," rad"),
			\elevation -> ControlSpec(pi.neg/2, pi/2, \lin, 0, 0, " rad")
		]);
	}

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
	plot{ |...args|
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

	//very slow...
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

		xmap = { |x| x.linlin(0, hsize-1, surface.manifold.rangeU[0], surface.manifold.rangeU[1]) };
		ymap = { |y| y.linlin(0, vsize-1, surface.manifold.rangeV[0], surface.manifold.rangeV[1])};

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

	*selfSwitchPeriodically { |t, g, numSecsSig ...signals|
		var allSignals = signals.prependI( numSecsSig );

		var f = { |valuesAtLastSwitchTime|
			//we create a new local time signal starting from 0;
			var numSecs = valuesAtLastSwitchTime[0];
			var localT = t.integral1;
			var output = g.(*[localT]++valuesAtLastSwitchTime);

			{ |x, t...sigCurrentValues|

				T(x,  if( t>=numSecs ){Some(sigCurrentValues)}{None()} )

			}.lift.(*[output, localT]++allSignals);
		};

		var startValues = allSignals.collect(_.now);
		^f.selfSwitch( startValues );
	}

	//bulti-in functions
	//for fixed rotations I could rotate the points before using them
	rotate { //angle1, 2, 3 -pi/2, pi/2
		^PField{ |u, v, t, rotate=0, tilt=0, tumble=0...args|
			var newP = UnitSpherical(u,v).asCartesian.rotate(rotate).tilt(tilt).tumble(tumble).asSpherical;
			this.func.valueArray( [newP.theta, newP.phi, t]++args )
		}
	}

	*prBump{ ^{ |x| 2**((1-x.squared).reciprocal.neg)*2 } }

	//SPOTLIGHT
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

	//BAR
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
		var center = surface.vcenter;
		^{ |u, v, t, widness|
			if( (center - v).abs < (widness * d) ) {
				1.0
			} {
				0.0
			}
		}
	}

	//EXPAND CONTRACT
	*expandContract{
		^PField( this.expandContractFunc(ImmDef.currentSurface) )
	}

	//this is kind of unique to closed surfaces.
	*expandContractFunc { |surface|
		var f = PField.spotlightFunc(surface);
		^{|u1, v1, t, u2, v2, c, curve|
			var xs = [ 0, 1, -99, -99, 1, 1, 5, curve ];
			if(c < 0.5){
				f.(u1, v1, t, u2, v2, xs.envAt( c*2 ) )
			} {
				f.(u1, v1, t, u2+pi, v2.neg, xs.envAt( 1 - (2*(c-0.5)) ) ) //optimized linlin
			}
		}
	}

	*constant { |value|
		^PField({ value })
	}

	//GRADIENT
	*gradient {
		^PField( this.gradientFunc( ImmDef.currentSurface ) )
	}

	*gradientFunc { |surface|
		var distFunc = surface.distFunc;
		var maxDist = surface.maxDist;

		^{ |u, v, t, u2=0.0, v2=0.0, a=0.0, b=1.0, curve=0|
			var env = [ 0, 1, -99, -99, 1, 1, 5, curve ];
			var x = env.envAt( distFunc.(u, v, u2, v2)/maxDist );
			(a*(1-x)) + (b*x)
		}
	}

	*gradientF { |f|
		^PField( this.gradientFFunc( ImmDef.currentSurface, f ) )
	}

	*gradientFFunc { |surface, f|
		var distFunc = surface.distFunc;
		var maxDist = surface.maxDist;

		^{ |u, v, t, u2=0.0, v2=0.0, a=0.0, b=1.0|
			var x = distFunc.(u, v, u2, v2)/maxDist;
			f.(x).linlin(0.0,1.0,a,b)
		}
	}

	//SPHERICAL HARMONICS
	*sphericalHarmonic{ |m,l|
		//double factorial
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

	//RANDOM HILLS
	*generateHillsFunc {
		^{ |n, s, sizeA=0.3, sizeB=0.5, bumpSize=0.5, heightA=1.0, heightB=1.0|
			n.collect{
				var tau = 2*pi;
				var u2 = rrand(s.manifold.rangeU[0].asFloat, s.manifold.rangeU[1].asFloat);
				var v2 = rrand(s.manifold.rangeV[0].asFloat, s.manifold.rangeV[1].asFloat);
				var size = rrand(sizeA.asFloat, sizeB.asFloat);
				var height = rrand(heightA.asFloat, heightB.asFloat);
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
			var u2 = rrand(s.manifold.rangeU[0].asFloat, s.manifold.rangeU[1].asFloat);
			var v2 = rrand(s.manifold.rangeV[0].asFloat, s.manifold.rangeV[1].asFloat);
			var size = rrand(sizeA.asFloat, sizeB.asFloat);
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
			var ndffdgd = rrand(0,1000);
			#oldState, n, nextnumHills, nextsizeA, nextsizeB, nextbumpSize, nexthA, nexthb = xs;

			nextnumHills = nextnumHills.asInteger;
			//"Running switch function again realt: %".format(t.now).postln;
			//we create a new set of hills to morph to:
			newState = T( oldState.at2, generateHillsFunc.(nextnumHills, surface, nextsizeA, nextsizeB, nextbumpSize, nexthA, nexthb) );
			a = newState.at1;
			b = newState.at2;

			//we create a new local time signal starting from 0;
			localT = t.integral1;
			//localT.collect{ |t| "% : %".format(ndffdgd,t).postln };

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
		^f.selfSwitch( [ T( generateHillsFunc.(*startValues2), generateHillsFunc.(*startValues2) ) ]++startValues )/*.enDebug("self")*/;
	}

	*randomHills { | t, numSecs=5.0, numHills = 5, sizeA=0.3, sizeB=0.5, bumpSize = 0.5, heightA=1.0, heightB=1.0|
		//this.checkArgs(\PField, \randomHills,
			//[t, numSecs, numHills, sizeA, sizeB, bumpSize], [FPSignal]++(SimpleNumber ! 5));
		^this.randomPatchGeneral( this.generateHillsFunc, ImmDef.currentSurface, t,
			numSecs, numHills, sizeA, sizeB, bumpSize, heightA, heightB )
	}

	*randomHills2 { | t, numSecsLo=0.5, numSecsHi=4, numHills = 5, sizeA=0.3, sizeB=0.5, bumpSize = 0.5, heightA=1.0, heightB=1.0|
		//this.checkArgs(\PField, \randomHills,
			//[t, numSecs, numHills, sizeA, sizeB, bumpSize], [FPSignal]++(SimpleNumber ! 5));
		var numSecs = { |t, lo, hi| rrand(lo.asFloat, hi.asFloat) }.lift.(t, numSecsLo, numSecsHi);

		^this.randomPatchGeneral( this.generateHillsFunc, ImmDef.currentSurface, t,
			numSecs, numHills, sizeA, sizeB, bumpSize, heightA, heightB )
	}

	*randomHillsBipolar {| t, numSecs, numHills = 5, sizeA=0.3, sizeB=0.5, bumpSize = 0.5|
		//this.checkArgs(\PField, \randomHillsBipolar,
			//[t, numSecs, numHills, sizeA, sizeB, bumpSize], [FPSignal]++(SimpleNumber ! 5));
		^this.randomPatchGeneral( this.generateHillsBipolarFunc, ImmDef.currentSurface,
			t, numSecs, numHills, sizeA, sizeB, bumpSize )
	}

	*continousRandomSpotlight3{ |t, numSecs, curve = 0|
		var check = this.checkArgs(\PField, \continousRandomSpotlight,
			[t, numSecs], [FPSignal, SimpleNumber] );
		var surface = ImmDef.currentSurface;
		//time wrapping around numSecs
		var t2 = t.collect{ |t| t.mod(numSecs) / numSecs };
		var randomSpotLight = {
			PField.spotlightFixedFunc( surface, rrand(0, 2pi), rrand(pi.neg,pi) )
		};
		var env = Env([0,1],[1],curve).asMultichannelArray[0];
		var changefuncEvent = t2
		.changes
		.storePrevious
		.select{ |tup| (tup.at2 < 0.2) && (tup.at1 > (0.8) ) }
		.hold(0.0).collect( randomSpotLight );

		var f = { |h|
			PField({ |u, v, t|
				if(t < 0.5) {
					h.(u,v,0,env.envAt(1-(t*2)),0.2)
				} {
					h.(u,v,0,env.envAt((t-0.5)*2),0.2)
				}
			}).(t2)
		};

		//event switching
		^T(changefuncEvent.switch2( f ), changefuncEvent)
	}

	//Continous Random Spotlight
	*continousRandomSpotlight{ |t, numSecs, curve = 0|
		var check = this.checkArgs(\PField, \continousRandomSpotlight,
			[t, numSecs, curve], [FPSignal, [SimpleNumber, FPSignal], [SimpleNumber, FPSignal]] );

		var surface = ImmDef.currentSurface;

		var g = { |t, numSecs|
			var h = PField.spotlightFixedFunc( surface, rrand(0, 2pi), rrand(pi/2.neg,pi/2) );
			PField({ |u, v, t, curve|
				var env = [ 0, 1, -99, -99, 1, 1, 5, curve ];
				if(t < 0.5) {
					h.(u,v,0,env.envAt(1-(t*2)),0.2)
				} {
					h.(u,v,0,env.envAt((t-0.5)*2),0.2)
				}
			}).(t / numSecs, curve.asFPSignal)
		};

		^this.selfSwitchPeriodically(t, g, numSecs.asFPSignal)
	}

	*continousRandomSpotlight2{ |t, numSecsLo, numSecsHi, curve = 0|
		var check = this.checkArgs(\PField, \continousRandomSpotlight,
			[t, numSecsLo, numSecsHi, curve],
			[FPSignal, [SimpleNumber, FPSignal], [SimpleNumber, FPSignal],[SimpleNumber, FPSignal]] );

		var numSecs = { |t, lo, hi| rrand(lo, hi) }.lift.(t, numSecsLo, numSecsHi);
		^this.continousRandomSpotlight(t, numSecs, curve)

	}

	//Move Hills
	*moveHills { |t, numSecs, numHills = 5, size = 0.4, step = 0.4, startInSamePlace = true|

		var numSecsSig = numSecs.asFPSignal;
		var numHillsSig = numHills.asFPSignal;
		var sizeSig = size.asFPSignal;
		var stepSig = step.asFPSignal;

		var surface = ImmDef.currentSurface;
		var crossfade = { |x, a, b| ( a * (1-x) ) + (b * x) };
		var iteratePoints = { |ps, r=0.4| ps.collect{ |t| t |+| T( rrand( r.neg, r), rrand( r.neg, r) ) }};
		var genPoints = { |n| T( rrand(0, 2pi), rrand(-pi, pi) ) ! n };
		var genPoints2 = { |n| { T( rrand(0, 2pi), rrand(-pi, pi) ) } ! n };
		var updateState = { |oldPoints, step = 0.4|  T( oldPoints, iteratePoints.(oldPoints, step) ) };

		//this morphs from function A to function B
		var f = { |xs|

			var oldState, nextNumSecs, nextNumHills, nextStep, newState, localTSig, g;
			#oldState, nextNumSecs, nextNumHills, nextStep = xs;
			nextNumHills = nextNumHills.asInteger;
			//"Running switch function again realt: %".format(t.now).postln;

			newState = updateState.( oldState.at2, nextStep );

			//we create a new local time signal starting from 0;
			localTSig = t.integral1;

			g = { |time, numSecs, numHills, size, step|
				var pos = crossfade.(time/nextNumSecs, newState.at1, newState.at2);

				var funcs = pos.collect{ |p|
					PField.spotlightFixedFunc(surface, p.at1, p.at2)
				};

				var output = surface.points.collect{ |v|
					funcs.collect{ |f|
						f.(v[0], v[1], time, size, 0.5)
					}.sum.min(1.0)
				};

				T(output, if((time>=nextNumSecs)){Some([newState, numSecs, numHills, step])}{None()})
			};

			g.lift.(localTSig, numSecsSig, numHillsSig, sizeSig, stepSig);
		};

		//event switching
		//calling .now is not pure...
		var startValues = [numSecsSig, numHillsSig, stepSig].collect(_.now);
		var initFs = updateState.( if(startInSamePlace) {
			genPoints
		} {
			genPoints2
		}.(numHillsSig.now),
		stepSig.now );

		^f.selfSwitch( [initFs]++startValues );
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
		var vrateT = t.changeRate(freq.asFPSignal);
		^if( plot ){
			PSmoothPlot(pf, vrateT, u0, v0, l)
		} {
			pf.(vrateT, u0, v0, l)
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