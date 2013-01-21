clear all

Z = csvread('master_sample.csv');

k = 100;
muV = zeros(1,k);
sigmaV = ones(1,k); % equal-variance

alphaV = 0.05;
n0 = 16; % first-stage sample
deltaV = sigmaV(1)/sqrt(n0);

% best system
muV(1) = deltaV;

a = -log(2*alphaV/(k-1));

Iset = ones(1,k);
Iready = zeros(1,k);
Y = zeros(6,k); % row 1 represents nb of sample, D_i; row 2 represents sum(Y_i);
% row 3 represents sum(Y_i^2); row 4 reps mean(Y_i);
% row 5 reps S_i^2, sample variance; row 6 reps S_i^2/D_i;

% at (row 1)th sample
% from alt (row 2)
% and alt (row 3) killed
% by alt (row 4)
eliminateAt = zeros(4, k);

ii = 1;

for i = 1:length(Z)
    if (Iset(Z(i,1)) == 0)
        continue
    end
    Y(1,Z(i,1)) = Y(1,Z(i,1)) + 1;
    Y(2,Z(i,1)) = Y(2,Z(i,1)) + Z(i,2);
    Y(3,Z(i,1)) = Y(3,Z(i,1)) + Z(i,2)^2;
    Y(4,Z(i,1)) = Y(2,Z(i,1)) / Y(1,Z(i,1));
    Y(5,Z(i,1)) = Y(3,Z(i,1)) / (Y(1,Z(i,1))-1) - Y(2,Z(i,1))^2 / (Y(1,Z(i,1))-1) / (Y(1,Z(i,1)));
    Y(6,Z(i,1)) = Y(5,Z(i,1)) / Y(1,Z(i,1));
    
    if (Y(1,Z(i,1)) == n0)
        Iready(Z(i,1)) = Z(i,1);
    end
          
    Iold = Iready(Iready>0);
    Ilength = length(Iold);
    if ( Ilength > 1 && Iready(Z(i,1)) > 0)
        for j=1:Ilength
            if(Iold(j) ~= Z(i,1))
                tempij = Y(4,Iold(j))-Y(4,Z(i,1));
                tempComij = (Y(6,Iold(j))+Y(6,Z(i,1)))*(-a/deltaV)+0.5*deltaV;
                if(tempij <= min(0, tempComij))
                    Iset(Iold(j)) = 0;
                    Iready(Iold(j)) = 0;
                    eliminateAt(1, ii) = i;
                    eliminateAt(2, ii) = Z(i, 1);
                    eliminateAt(3, ii) = Iold(j);
                    eliminateAt(4, ii) = Z(i, 1);
                    ii = ii + 1;
                elseif(tempij >= - min(0,tempComij))
                    if Iset(Z(i,1)) > 0
                        Iset(Z(i,1)) = 0;
                        Iready(Z(i,1)) = 0;
                        eliminateAt(1, ii) = i;
                        eliminateAt(2, ii) = Z(i, 1);
                        eliminateAt(3, ii) = Z(i, 1);
                        eliminateAt(4, ii) = Iold(j);
                        ii = ii + 1;
                    end
                end
            end
        end
    end   
end
