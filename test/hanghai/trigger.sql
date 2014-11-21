delimiter $
create trigger tri_update_table_netincome
after insert on table_incomeentry
for each row
begin
declare @v_avg_time int;
declare @v_avg_money int;
declare @v_ratio float;

select avg(table_incomeentry.timeinsecond) from table_incomeentry where fromport=new.fromport and toport=new.toport;
select avg(table_incomeentry.money) from table_incomeentry where fromport=new.fromport and toport=new.toport;
set @v_ratio = @v_avg_time *1.0 / @v_avg_money;

if exists(select * from table_netincome where fromport=new.fromport and toport=new.toport) then
update table_netincome set timeinsecond=@v_avg_time, money=@v_avg_money, moneytimeratio=@v_ratio where fromport=new.fromport and toport=new.toport;
else
insert into table_netincome values(new.fromport,new.toport,@v_avg_time,@v_avg_money,@v_ratio);
end;
end;
$
delimiter ;