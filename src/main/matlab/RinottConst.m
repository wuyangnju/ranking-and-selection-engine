function rc = RinottConst(alpha, k, n0)

probp = 1 - alpha;

npoint=32; % using 32 point Gauss-Laguerre numerical quadrature (Wilcox 1984)
[rootx,weight]=GaussLaguerre_2(npoint,0);
y=rootx;

hlow = 1.0;
hupp = 10.0;
htemp = (hlow + hupp) / 2;

% Gauss-Laguerre numerical quadrature method to compute the integral (see Wilcox 1984)
G=zeros(npoint,1);
for j=1:npoint
    intervalue=zeros(npoint,1);

    for i=1:npoint
        intervalue(i)=normcdf(htemp/sqrt((n0-1)*(1/rootx(i)+1/y(j))))*exp(rootx(i))*chi2pdf(rootx(i),n0-1);
    end

    G(j)=exp(y(j))*(dot(intervalue,weight))^(k-1)*chi2pdf(y(j),n0-1);        
end
ptemp=dot(G,weight);

% repeat it until the stopping rule is satisfied
while(abs(ptemp-probp) > 1.0e-4)
    if(ptemp > probp)
        hupp = htemp;
    else
        hlow = htemp;
    end
    htemp = (hlow + hupp) / 2;

    G=zeros(npoint,1);
    for j=1:npoint            
        intervalue=zeros(npoint,1);

        for i=1:npoint
            intervalue(i)=normcdf(htemp/sqrt((n0-1)*(1/rootx(i)+1/y(j))))*exp(rootx(i))*chi2pdf(rootx(i),n0-1);
        end

        G(j)=exp(y(j))*(dot(intervalue,weight))^(k-1)*chi2pdf(y(j),n0-1);            
    end
    ptemp=dot(G,weight);
end
rc = k*htemp^2;