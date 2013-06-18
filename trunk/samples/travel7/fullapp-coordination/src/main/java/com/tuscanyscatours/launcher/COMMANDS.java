package com.tuscanyscatours.launcher;

/**
 * 
 * @Author JiangWang<jiang.wang88@gmail.com>
 *
 */
public enum COMMANDS {
	/** simply access specified times without update*/
	access,
	/** simply update the component without accessing */
	update,
	/** access the component and execute update the component specified times */
	updateAt,
	/** help of the commands */
	help,
	/** used to disruption experiment */
	disruption,
	/** used to timeliness experiment */
	timeliness,
	/** used to overhead experiment */
	overhead,
	/** used to correctness experiment */
	correctness,
	/** get the execution recorder */
	ger,
	/** exit */
	exit;
}
