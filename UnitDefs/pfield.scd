UMapDef(\pfield, { |range = #[0.0,1.0]| UMapOut.kr( \control.kr(0.0).linlin(0.0,1.0,range[0],range[1])) })
.category_('ImmLib')
.mappedArgs_( [ \range ] );