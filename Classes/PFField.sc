/*
		(C)opyright 2013-2015 by Miguel Negrão

    This file is part of ImmLib.

		ImmLib is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

		ImmLib is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImmLib.  If not, see <http://www.gnu.org/licenses/>.
*/

/*

(
x = PField{ |p,t,c| t + c };
y = PField{ |p,t,c| t * c };
z = x + y
)

(
x = PField{ |p,t,c| t + c }.squared
)

p = PSphere(2)

(
x = PField{ |p,t,c| t + c }.linlin(0.0,1.0,0.0,10.0);
x.valueS(p, Var(0.1), Var(0.3) ).do(postln(_))
)

(
x = PField{ |p,t,c| t + c }.linlin(0.0,1.0,0.0,10.0);
x.valueArrayS( p,  Var(0.1), Var(0.3) ).do(postln(_))
)

(
x = PField{ |p,t,c| t + c };
p = PSphere(2);
x.valueArrayS( p,  Var(0.1), Var(0.3) ).do(postln(_))
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

	//to make it easy to turn on and off plotting
	plot{ |...args|
		^PSmoothPlot(*args.prependI(this))
	}

	//plotSignal FPSignal carrying 0 or 1 to turn on and off plot
	plotOnOff{ |plotSignal...args|
		^PField.plotOnOffFunc( plotSignal ).(this, *args);
	}

	*plotOnOffFunc{ |plotSignal|
		var plotBool = plotSignal.collect(_.booleanValue);
		var plot = PSmoothPlot.basicNew;
		var x1 = plotBool.changes.onlyChanges.collect{ |v| IO{
			if(v){plot.startRenderer}{plot.stopRenderer}
		}}.enOut;
		var sendColors = { |v| IO{
			plot.sendMsg(* (["/colors"]++([v,plot.surface.points].flopWith{ |c,xs|
				[0.0, c.asFloat, (1-c).asFloat]
			}.flat)))
		}
		};
		var c = 0;
		^{ |pf...args|
			//var x2 = "doing plotOnOffFunc v %".format(c).postln;
			var tEventSource = args[0].changes;
			var outSignal = pf.(*args);
			var outSignal2 = sendColors.(_) <%> pf.valueS(plot.surface, *args);
			c = c + 1;
			(outSignal2  <@ plotBool.when(tEventSource) ).enOut;
			outSignal
		}
	}

	*plotOnOffFuncMerged{ |plotSignal|
		var plotBool = plotSignal.collect(_.booleanValue);
		var plot = PSmoothPlot.basicNew;
		var x1 = plotBool.changes.onlyChanges.collect{ |v| IO{
			if(v){plot.startRenderer}{plot.stopRenderer}
		}}.enOut;
		var sendColors = { |v| IO{
			plot.sendMsg(* (["/colors"]++([v,plot.surface.points].flopWith{ |c,xs|
				[0.0, c.asFloat, (1-c).asFloat]
			}.flat)))
		}
		};
		var c = 0;
		^{ |pf...args|
			//var x2 = "doing plotOnOffFunc v %".format(c).postln;
			var tEventSource = args[0].changes;
			var outSignal = pf.(*args);
			var outSignal2 = sendColors.(_) <%> pf.valueS(plot.surface, *args);
			c = c + 1;
			T(_,_) <%> outSignal <*> (outSignal2  <@ plotBool.when(tEventSource) ).hold(IO{});
		}
	}

	*plotOnOffFuncSeparate{ |plotSignal|
		var plotBool = plotSignal.collect(_.booleanValue);
		var plot = PSmoothPlot.basicNew;
		var x1 = plotBool.changes.onlyChanges.collect{ |v| IO{
			if(v){plot.startRenderer}{plot.stopRenderer}
		}}.enOut;
		var sendColors = { |v| IO{
			plot.sendMsg(* (["/colors"]++([v,plot.surface.points].flopWith{ |c,xs|
				[0.0, c.asFloat, (1-c).asFloat]
			}.flat)))
		}
		};
		var c = 0;
		^T({ |pf...args|
			pf.(*args)
		},
		{ |pf...args|
			//var x2 = "doing plotOnOffFunc v %".format(c).postln;
			var tEventSource = args[0].changes;
			var outSignal2 = sendColors.(_) <%> pf.valueS(plot.surface, *args);
			c = c + 1;
			(outSignal2  <@ plotBool.when(tEventSource)).hold(IO{})
		})
	}

	//problem with sc tcp that connections are not closed
	plotOnOffStatic{ |plotSignal...args|
		var plotBool = plotSignal.collect(_.booleanValue);
		var plot = PSmoothPlot.basicNew;
		var outSignal2 = (sendColors.(_) <%> this.valueS(*([plot.surface,Var(0)]++args) ) );
		var x1 = (T(_,_) <%> outSignal2 <@> plotBool.changes.onlyChanges).collect{ |t|
			if(t.at2){IO{ plot.startRenderer(true, { t.at1.unsafePerformIO }) }}{plot.stopRendererIO}
		}.enOut;
		var sendColors = { |v| IO{
			plot.sendMsg(* (["/colors"]++([v,plot.surface.points].flopWith{ |c,xs|
				[0.0, c.asFloat, (1-c).asFloat]
			}.flat)))
		}};
		var outSignal = this.value(*([Var(0)]++args));
		var forPlotting = plotBool.when(outSignal2.changes);
		forPlotting.enOut;
		//forPlotting.enDebug("forPlotting");
		//outSignal.enDebug("outsignal");
		^outSignal
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
	rotate3D { //angle1, 2, 3 -pi/2, pi/2
		^PField{ |u, v, t, rotate=0, tilt=0, tumble=0...args|
			var newP = UnitSpherical(u,v).asCartesian.rotate(rotate).tilt(tilt).tumble(tumble).asSpherical;
			this.func.valueArray( [newP.theta, newP.phi, t]++args )
		}
	}

	rotate2D {
		^PField{ |u, v, t, angle=0...args|
			//from Point
			var sinr = angle.sin;
			var cosr = angle.cos;
			var u2 = (u * cosr) - (v * sinr);
			var v2 = (v * cosr) + (u * sinr);
			this.func.valueArray( [u2+0.5, v2+0.5, t]++args )
		}
	}

	rotTransScale2D {
		^PField{ |u, v, t, angle=0, scale=1, ut=0, vt=0...args|
			//from Point
			var sinr = angle.sin;
			var cosr = angle.cos;
			var u2 = scale * (u-0.5);
			var v2 = scale * (v-0.5);
			var u3 = (u2 * cosr) - (v2 * sinr);
			var v3 = (v2 * cosr) + (u2 * sinr);
			this.func.valueArray( [u3+0.5-ut, v3+0.5-vt, t]++args )
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

	*spotlightInverse{
		^PField( this.spotlightFuncInverse( ImmDef.currentSurface ) )
	}

	*spotlightFuncInverse{ |surface|
		var bump = this.prBump;
		var distFunc = surface.distFunc;
		var maxDist = surface.maxDist;

		^{ |u1, v1, t, u2, v2, c, d=0.2|
			var dist = distFunc.(u1, v1, u2, v2);
			var c2 = c.linlin(0.0,1.0, d.neg, 1.0);
			var cpi = c2 * maxDist;
			var cpid = cpi + (d*maxDist);
			if( dist < cpi ) {
				0
			} {
				if( dist < cpid ) {
					bump.( dist.linlin(cpi,cpid,1.0,0.0) )
				} {
					1
				}
			}
		}

	}

	//BAR
	*barU{
		^PField( this.barFuncU( ImmDef.currentSurface ) )
	}

	*barFuncU { |surface|
		var d = surface.du/2;
		var center = surface.ucenter;
		^{ |u, v, t, wideness=0|
			if( (center - u).abs <= (wideness * d) ) {
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
			if( (center - v).abs <= (widness * d) ) {
				1.0
			} {
				0.0
			}
		}
	}

	//EXPAND CONTRACT
	//spherical only
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
			//var env = [ 0, 1, -99, -99, 1, 1, 5, curve ];
			//var x = env.envAt( distFunc.(u, v, u2, v2)/maxDist );
			//(a*(1-x)) + (b*x)
			[ a, 1, -99, -99, b, maxDist, 5, curve ].envAt( distFunc.(u, v, u2, v2) );
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

	//this is only valid for planes
	*gradient1D {
		^PField( this.gradient1DFunc( ImmDef.currentSurface ) )
	}

	*gradient1DFunc { |surface|
		var maxDist = 1.hypot(1);
		var halfmaxDist = maxDist / 2;
		var center = RealVector2D[ 0.5, 0.5 ];
		^{ |u, v, t, angle=0, vala=0.0, valb=1.0, curve=0|
			var theta = angle * 2pi;
			var sina = sin(theta);
			var cosa = cos(theta);
			var a = RealVector2D[halfmaxDist * cosa, halfmaxDist * sina ] + center;
			var p = RealVector2D[ u, v ];
			var n = RealVector2D[ sina.neg, cosa ];
			var amp = a - p;
			var d = ( amp - (n * (amp <|> n)) ).norm;
			[ vala, 1, -99, -99, valb, maxDist, 5, curve ].envAt( d );
			//d / maxDist
		}
	}

	//SPHERICAL HARMONICS
	//for spherical range
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

	*sphericalHarmonicFunc{
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

		^{ |m,l|
			var shfunc = simplifiedsh.(m,l);
			PField({ |u, v, t,f=3|
				(shfunc.( u, (pi/2)-v)*cos(2pi*f*t)).linlin(-1.0,1.0,0.0,1.0)
			})
		}
	}

	//for any range
	*sphericalHarmonicNormalized{ |m,l|
		var surface = ImmDef.currentSurface;
		var manifold = surface.manifold;
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
			(shfunc.( manifold.toNormalizedU(u).linlin(0.0,1.0,0,2pi), (pi/2)-manifold.toNormalizedV(v).linlin(0.0,1.0,-pi/2,pi/2))*cos(2pi*f*t)).linlin(-1.0,1.0,0.0,1.0)
		})
	}

	*sphericalHarmonicNormalizedFunc{
		var surface = ImmDef.currentSurface;
		var manifold = surface.manifold;
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
		^{ |m,l|
			var shfunc = simplifiedsh.(m,l);
			PField({ |u, v, t,f=3|
			(shfunc.( manifold.toNormalizedU(u).linlin(0.0,1.0,0,2pi), (pi/2)-manifold.toNormalizedV(v).linlin(0.0,1.0,-pi/2,pi/2))*cos(f*t)).linlin(-1.0,1.0,0.0,1.0)
		})
		}
	}

	//reactive fields (i.e. using FRP switching)

	//RANDOM HILLS
	*generateHillsFunc {
		^{ |n, s, sizeA=0.3, sizeB=0.5, bumpSize=0.5, heightA=1.0, heightB=1.0|
			n.collect{
				var u2 = rrand(s.manifold.rangeU[0].asFloat, s.manifold.rangeU[1].asFloat);
				var v2 = rrand(s.manifold.rangeV[0].asFloat, s.manifold.rangeV[1].asFloat);
				var size = rrand(sizeA.asFloat, sizeB.asFloat);
				var height = rrand(heightA.asFloat, heightB.asFloat);
				var f = PField.spotlightFixedFunc(s, u2, v2);
				{ |u,v,t|
					//PFFuncs.growArea(UnitSpherical(theta, phi)).(p,size,0.5)
					f.(u,v,nil,size, bumpSize) * height
				}
			}.sum / n //this is also a function
		}
	}

	//Random Hills bipolar
	*generateHillsBipolarFunc {
		^{ |n, s, sizeA=0.3, sizeB=0.5, bumpSize=0.5| (n.collect{
			var u2 = rrand(s.manifold.rangeU[0].asFloat, s.manifold.rangeU[1].asFloat);
			var v2 = rrand(s.manifold.rangeV[0].asFloat, s.manifold.rangeV[1].asFloat);
			var size = rrand(sizeA.asFloat, sizeB.asFloat);
			var sig = [-1,1].choose;
			var f = PField.spotlightFixedFunc(s, u2, v2);
			{ |u,v,t|
				f.(u,v,nil,size, bumpSize) * sig
			}
		}.sum / n)*0.5 + 0.5 //this is also a function
		}
	}

	*generateHillsFuncDeterministic {
		^{ |s, bumpSize=0.5, hillsData|
			hillsData.collect{ |xs|
				var u2, v2, size, height, f;
				#u2, v2, size, height = xs;
				f = PField.spotlightFixedFunc(s, u2, v2);
				{ |u,v,t|
					f.(u,v,nil,size, bumpSize) * height
				}
			}.sum / hillsData.size //this is also a function
		}
	}

	*randomPatchGeneral { |generateHillsFunc, surface, t, numSecs, numHills = 5, sizeA=0.3, sizeB=0.5, bumpSize = 0.5, heightA=1.0, heightB=1.0, plot = false|
		var numSecsSig = numSecs.asFPSignal;
		var numHillsSig = numHills.asFPSignal;
		var sizeASig = sizeA.asFPSignal;
		var sizeBSig = sizeB.asFPSignal;
		var bumpSizeSig = bumpSize.asFPSignal;
		var heightASig = heightA.asFPSignal;
		var heightBSig = heightB.asFPSignal;
		var plotFunc = if(plot){ PSmoothPlot.reusable }{ { |pf...args| pf.(*args) } };

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

			pfSig = plotFunc.( PField({ |u, v, t|
				var t2 = t/n;
				( (1-t2) * a.(u,v)  ) + (t2 * b.(u,v))
			}), localT);

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

	*randomPatchGeneral2 { |generateHillsFunc, surface, t, numSecs, numHills = 5, sizeA=0.3, sizeB=0.5, bumpSize = 0.5, heightA=1.0, heightB=1.0, plot=0.0|
		var numSecsSig = numSecs.asFPSignal;
		var numHillsSig = numHills.asFPSignal;
		var sizeASig = sizeA.asFPSignal;
		var sizeBSig = sizeB.asFPSignal;
		var bumpSizeSig = bumpSize.asFPSignal;
		var heightASig = heightA.asFPSignal;
		var heightBSig = heightB.asFPSignal;
		var plotSignal = plot.asFPSignal;

		var plotFunc = PField.plotOnOffFuncMerged(plotSignal);

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

			pfSig = plotFunc.( PField({ |u, v, t|
				var t2 = t/n;
				( (1-t2) * a.(u,v)  ) + (t2 * b.(u,v))
			}), localT);

			{ |x, t, nnumSecs, nnumHills, nsizeA, nsizeB, nbumpSize, nha, nhb|
				T(x, if((t>=n)){Some([newState, nnumSecs, nnumHills, nsizeA, nsizeB, nbumpSize, nha, nhb])}{None()}) }
			.lift.(pfSig, localT, numSecsSig, numHillsSig, sizeASig, sizeBSig, bumpSizeSig, heightASig, heightBSig);
		};

		//event switching
		//calling .now is not pure...
		var startValues = [numSecsSig, numHillsSig, sizeASig, sizeBSig, bumpSizeSig, heightASig, heightBSig].collect(_.now);
		var startValues2 = [startValues[1].asInteger, surface]++startValues[2..];
		var result = f.selfSwitch( [ T( generateHillsFunc.(*startValues2), generateHillsFunc.(*startValues2) ) ]++startValues )/*.enDebug("self")*/;
		result.collect(_.at2).enOut;
		^result.collect(_.at1)
	}

	*randomPatchDeterministic { |surface, randomValuesArray, generateHillsFunc, t, numSecs,
		bumpSize = 0.5|

		var numSecsSig = numSecs.asFPSignal;
		var bumpSizeSig = bumpSize.asFPSignal;
		var sizeOfRandoms = randomValuesArray.size;

		//this morphs from function A to function B
		var f = { |xs|

			var previousHillsTuple, nextHillsTuple, currentNumSeconds, currentbumpSize,
			localT, pfSig, hillsA, hillsB, counter, hillsData;
			//var rand;
			#previousHillsTuple, currentNumSeconds, currentbumpSize, counter = xs;

			//very very very bad and ugly !!!
			//fix this somehow
			//prorbably should recompile network every time we press play ? Then we lose state, is that good ?
			/*t.collect{ |t|
				if( t == 0.0 ) {
					//"time back to zero".postln;
					counter = 0;
					/*#u2, v2, size, height = randomValuesArray[0];
					oldHills =  generateHillsFunc.(surface, nextnumHills, nextbumpSize, u2, v2, size, height);
					#u2, v2, size, height = randomValuesArray[1];
					newHills = generateHillsFunc.(surface, nextnumHills, nextbumpSize, u2, v2, size, height);
					newState = T( oldHills, newHills );*/
				}
			};*/

			//"Running switch function again realt: %".format(t.now).postln;
			//we create a new set of hills to morph to:
			hillsA = previousHillsTuple.at2;
			hillsData = randomValuesArray[counter];
			hillsB = generateHillsFunc.(surface, currentbumpSize, hillsData);
			nextHillsTuple = T( hillsA, hillsB );

			//we create a new local time signal starting from 0;
			localT = t.integral1;
			//localT.collect{ |t| "% : %".format(ndffdgd,t).postln };
			pfSig = PField({ |u, v, t|
				var t2 = t/currentNumSeconds;
				( (1-t2) * hillsA.(u,v)  ) + (t2 * hillsB.(u,v))
			}).(localT);

			//rand = rrand(0,100000);
			//"counter: % %".format(counter, rand).postln;

			{ |x, currentTime, nextNumSecs, nextBumpSize|
				T(x, if((currentTime>=currentNumSeconds)){Some([nextHillsTuple, nextNumSecs, nextBumpSize, (counter+1).mod(sizeOfRandoms)])}{None()}) }
			.lift.(pfSig, localT, numSecsSig, bumpSizeSig);
		};

		//event switching
		//calling .now is not pure...
		var startValues = [numSecsSig.now.asInteger, bumpSizeSig.now];
		var startHills = generateHillsFunc.(surface, bumpSizeSig.now, randomValuesArray[0]);
		^f.selfSwitch( [ T( nil, startHills ) ]++startValues++[1] );//.enDebug("self");
	}

	*randomHills { | t, numSecs=5.0, numHills = 5, sizeA=0.3, sizeB=0.5, bumpSize = 0.5, heightA=1.0, heightB=1.0, plot = false|
		//this.checkArgs(\PField, \randomHills,
			//[t, numSecs, numHills, sizeA, sizeB, bumpSize], [FPSignal]++(SimpleNumber ! 5));
		^this.randomPatchGeneral( this.generateHillsFunc, ImmDef.currentSurface, t,
			numSecs, numHills, sizeA, sizeB, bumpSize, heightA, heightB, plot )
	}

	*randomHillsPlot { | t, numSecs=5.0, numHills = 5, sizeA=0.3, sizeB=0.5, bumpSize = 0.5, heightA=1.0, heightB=1.0, plot = 0.0|
		//this.checkArgs(\PField, \randomHills,
			//[t, numSecs, numHills, sizeA, sizeB, bumpSize], [FPSignal]++(SimpleNumber ! 5));
		^this.randomPatchGeneral2( this.generateHillsFunc, ImmDef.currentSurface, t,
			numSecs, numHills, sizeA, sizeB, bumpSize, heightA, heightB, plot )
	}

	//randomValuesArray -> [ [u, v, size, height] ]
	//                     all of the same size, obviously
	*randomHillsDeterministic { | t, randomValuesArray, numSecs=5.0, bumpSize = 0.5|
		//this.checkArgs(\PField, \randomHills,
			//[t, numSecs, numHills, sizeA, sizeB, bumpSize], [FPSignal]++(SimpleNumber ! 5));
		^this.randomPatchDeterministic( ImmDef.currentSurface, randomValuesArray, this.generateHillsFuncDeterministic, t,
			numSecs, bumpSize )
	}

	*makeArrayForRandHillDeterm { |s, sizeSequence=20, numHills=3, sizeA=0.3, sizeB=0.5, heightA=1, heightB=1|
		^sizeSequence.collect{
			numHills.collect{
				var u2 = rrand(s.manifold.rangeU[0].asFloat, s.manifold.rangeU[1].asFloat);
				var v2 = rrand(s.manifold.rangeV[0].asFloat, s.manifold.rangeV[1].asFloat);
				var size = rrand(sizeA.asFloat, sizeB.asFloat);
				var height = rrand(heightA.asFloat, heightB.asFloat);
				[u2, v2, size, height]
			}
		}
	}

	*randomHills2 { | t, numSecsLo=0.5, numSecsHi=4, numHills = 5, sizeA=0.3, sizeB=0.5, bumpSize = 0.5, heightA=1.0, heightB=1.0, plot = false|
		//this.checkArgs(\PField, \randomHills,
			//[t, numSecs, numHills, sizeA, sizeB, bumpSize], [FPSignal]++(SimpleNumber ! 5));
		var numSecs = { |t, lo, hi| rrand(lo.asFloat, hi.asFloat) }.lift.(t, numSecsLo, numSecsHi);

		^this.randomPatchGeneral( this.generateHillsFunc, ImmDef.currentSurface, t,
			numSecs, numHills, sizeA, sizeB, bumpSize, heightA, heightB, plot )
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
	*continousRandomSpotlight{ |t, numSecs, curve = 0, plot = false|
		var check = this.checkArgs(\PField, \continousRandomSpotlight,
			[t, numSecs, curve], [FPSignal, [SimpleNumber, FPSignal], [SimpleNumber, FPSignal]] );

		var surface = ImmDef.currentSurface;
		var u_lo = surface.manifold.rangeU[0].asFloat;
		var u_hi = surface.manifold.rangeU[1].asFloat;
		var v_lo = surface.manifold.rangeV[0].asFloat;
		var v_hi = surface.manifold.rangeV[1].asFloat;
		var plotFunc = if(plot){ PSmoothPlot.reusable }{ { |pf...args| pf.(*args) } };

		var g = { |t, numSecs|
			var h = PField.spotlightFixedFunc( surface, rrand(u_lo, u_hi), rrand(v_lo, v_hi) );
			plotFunc.( PField({ |u, v, t, curve|
				var env = [ 0, 2, -99, -99, 1, numSecs/2, 5, curve, 0, numSecs/2, 5, curve.neg ];
				h.(u,v,0,env.envAt(t), 0.2)
			}), t, curve.asFPSignal )
		};

		^this.selfSwitchPeriodically(t, g, numSecs.asFPSignal)
	}

	*continousRandomSpotlight2{ |t, numSecsLo, numSecsHi, curve = 0, plot = false|
		var check = this.checkArgs(\PField, \continousRandomSpotlight,
			[t, numSecsLo, numSecsHi, curve],
			[FPSignal, [SimpleNumber, FPSignal], [SimpleNumber, FPSignal],[SimpleNumber, FPSignal]] );

		var numSecs = { |t, lo, hi| rrand(lo, hi) }.lift.(t, numSecsLo, numSecsHi);
		^this.continousRandomSpotlight(t, numSecs, curve, plot)

	}

	//Move Hills
	//modes = bounce | wrap | noconstrain
	*moveHills { |t, numSecs, numHills = 5, size = 0.4, step = 0.1, startInSamePlace = true, mode = \noconstrain|

		var t1 = if( [\bounce, \wrap, \noconstrain].includes(mode).not ){ Error("PField#moveHills - mode must be one of [\bounce, \wrap, \noconstrain] ").throw };
		var numSecsSig = numSecs.asFPSignal;
		var numHillsSig = numHills.asFPSignal;
		var numHillsLastValsSig = numHillsSig.inject( Tuple2(numHillsSig.now,numHillsSig.now),
			{ |state,x| Tuple2( state.at2, x ) });
		var sizeSig = size.asFPSignal;
		var stepSig = step.asFPSignal;

		var surface = ImmDef.currentSurface;
		var crossfade = { |x, a, b| ( a * (1-x) ) + (b * x) };
		var mod2 = { |lo,hi| var d = hi - lo; { |x| (x - lo).mod(d) + lo } };
		var u_lo = surface.manifold.rangeU[0].asFloat;
		var u_hi = surface.manifold.rangeU[1].asFloat;
		var v_lo = surface.manifold.rangeV[0].asFloat;
		var v_hi = surface.manifold.rangeV[1].asFloat;
		var wrapFuncU = switch(mode)
		{\bounce} { fold(_, u_lo, u_hi) }
		{\noconstrain}{ I.d }
		{\wrap} { mod2.(u_lo,u_hi) };
		var wrapFuncV = switch(mode)
		{\bounce} { fold(_, v_lo, v_hi) }
		{\noconstrain}{ I.d}
		{\wrap} { mod2.(v_lo,v_hi) };
		var iteratePoints = { |ps, r=0.4|
			var rneg = r.neg;
			ps.collect{ |t|	t |+| Tuple2(rrand( rneg, r),rrand( rneg, r)) }
		};
		/*var iteratePoints = switch(mode)
		{\bouce} {
			{ |ps, r=0.4|
				var rneg;
				ps.collect{ |t|
					Tuple2(
						(t.at1 + rrand( rneg, r)).fold(u_lo, u_hi),
						(t.at2 + rrand( rneg, r)).fold(v_lo, v_hi)
					)
				}
			}
		}
		{\noconstrain}{
			{ |ps, r=0.4|
				var rneg;
				ps.collect{ |t|	t |+| Tuple2(rrand( rneg, r),rrand( rneg, r)) }
			}
		}
		{\wrap} {
			var d1= "wrap".postln;
			var u_mod = mod2.(u_lo,u_hi);
			var v_mod = mod2.(v_lo,v_hi);
			{ |ps, r=0.4|
				var rneg = r.neg;
				ps.collect{ |t|
					Tuple2(
						u_mod.(t.at1 + rrand( rneg, r)),
						v_mod.(t.at2 + rrand( rneg, r))
					).postln
				}
			}
		};*/
		var genPoints = { |n|
			T( rrand(u_lo, u_hi), rrand(v_lo, v_hi) ) ! n };
		var genPoints2 = { |n| {
			T( rrand(u_lo, u_hi), rrand(v_lo, v_hi) ) } ! n };

		//this morphs from function A to function B
		var f = { |xs|

			var oldState, nextNumSecs, nextNumHills, nextStep, oldNumHills, newState, localTSig, g;
			#oldState, nextNumSecs, nextNumHills, oldNumHills, nextStep = xs;
			nextNumHills = nextNumHills.asInteger;
			//"Running switch function again realt: %".format(t.now).postln;

			if(nextNumHills != oldNumHills) {
				oldState = if(startInSamePlace) {
					genPoints
				} {
					genPoints2
				}.(nextNumHills);
			};
			newState = iteratePoints.(oldState, nextStep);

			//we create a new local time signal starting from 0;
			localTSig = t.integral1;

			{ |time, numSecs, numHills, numHillsLastVals, size, step|
				var pos = crossfade.(time/nextNumSecs, oldState, newState);

				//wrap here
				var funcs = pos.collect{ |p|
					PField.spotlightFixedFunc(surface, wrapFuncU.(p.at1), wrapFuncV.(p.at2) )
				};

				var output = surface.points.collect{ |v|
					funcs.collect{ |f|
						f.(v[0], v[1], time, size, 0.5)
					}.sum.min(1.0)
				};

				T(
					output,
					if((time>=nextNumSecs)){
						Some([newState, numSecs, numHills, nextNumHills, step ])
					}{
						None()
					}
				)
			}.lift.(localTSig, numSecsSig, numHillsSig, numHillsLastValsSig, sizeSig, stepSig);
		};

		//event switching
		//calling .now is not pure...
		var startValues = [numSecsSig, numHillsSig, numHillsSig, stepSig].collect(_.now);
		var initFs = if(startInSamePlace) {
			genPoints
		} {
			genPoints2
		}.(numHillsSig.now);

		^f.selfSwitch( [initFs]++startValues );
	}



	//sin(2pi * f * y)
	//y = x + ct
	//sin(2pi * f* (x + ct) )
	//sin(2pi * ( (f*x) + (f*c*t) ) )
	//SinOsc.ar( f*c, f*x

	*waveUSin {  |t, l, freq, plot = false|

		var pf = PField({ |u,v, t, l|
			sin( 2pi * ( (l*u) - t) ).linlin(-1.0,1.0,0.0,1.0)
		});
		^if( plot ){
			PSmoothPlot(pf, t.changeRate(freq), l)
		} {
			pf.(t.changeRate(freq), l)
		}

	 }

	*waveVSin { |t, l, freq, plot = false|
		var pf = PField({ |u,v, t, l|
			sin( 2pi * ( (l*v) - t) ).linlin(-1.0,1.0,0.0,1.0)
		});
		^if( plot ){
			PSmoothPlot(pf, t.changeRate(freq), l)
		} {
			pf.(t.changeRate(freq), l)
		}
	}

	*wave2DSin { |t, u0, v0, l, freq, plot|
		var surface = ImmDef.currentSurface;
		var distFunc = surface.distFunc;
		var maxDist = surface.maxDist;
		var pf = PField({ |u,v, t, u0, v0, l|
			sin( 2pi * ((l*distFunc.(u,v,u0,v0)/maxDist) - t) ).linlin(-1.0,1.0,0.0,1.0)
		});
		var vrateT = t.changeRate(freq.asFPSignal);
		^pf.plotOnOff(plot, vrateT, u0, v0, l)
	}

	*wave2DSaw { |t, u0, v0, l, freq, plot|
		var surface = ImmDef.currentSurface;
		var distFunc = surface.distFunc;
		var maxDist = surface.maxDist;
		var pf = PField({ |u,v, t, u0, v0, l|
			( (l*distFunc.(u,v,u0,v0)/maxDist) - t) % 1.0
		});
		var vrateT = t.changeRate(freq.asFPSignal);
		^pf.plotOnOff(plot, vrateT, u0, v0, l)

	}

	*wave2D { |t, u0, v0, l, freq, g, plot = false|
		var surface = ImmDef.currentSurface;
		var distFunc = surface.distFunc;
		var maxDist = surface.maxDist;
		var pf = PField({ |u,v, t, u0, v0, l|
			g.( (l*distFunc.(u,v,u0,v0)/maxDist) - t)
		});
		^if( plot ){
			PSmoothPlot(pf, t.changeRate(freq), u0, v0, l)
		} {
			pf.(t.changeRate(freq), u0, v0, l)
		}
	}

	*wave1DSin { |t, angle, l, freq, plotSignal|
		^PField({ |u,v, t, l|
			sin( 2pi * ( (l*u) - t) ).linlin(-1.0,1.0,0.0,1.0)
		}).rotate2D.plotOnOff(plotSignal, t.changeRate(freq), angle.nlin(0,2pi), l)
	}

	*wave1DSaw { |t, angle, l, freq, plotSignal|
		^PField({ |u,v, t, l|
			( (l*u) - t) % 1.0
		}).rotate2D.plotOnOff(plotSignal, t.changeRate(freq), angle.nlin(0,2pi), l)
	}

	*wave1D { |t, angle, l, freq, g, plotSignal|
		^PField({ |u,v, t, l|
			g.( (l*u) - t)
		}).rotate2D.plotOnOff(plotSignal, t.changeRate(freq), angle.nlin(0,2pi), l)
	}

}
